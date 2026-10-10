package com.bolehunter.extension.resume;

import com.bolehunter.model.CandidateDO;
import com.bolehunter.core.exception.ForbiddenException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简历解析器：本地规则解析（正则 + 关键词），不依赖外部服务。
 * 支持 PDF（pdfbox）、doc（poi-scratchpad）、docx（poi-ooxml）。
 * 提取字段：电话/邮箱/姓名/学历为高置信度规则；公司/职位尽力而为，失败留空由顾问补录。
 */
@Slf4j
@Component
public class ResumeParser {

    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.]+");
    private static final Pattern NAME_LABEL_PATTERN = Pattern.compile("姓\\s*名[:：]\\s*([\\u4e00-\\u9fa5]{2,4})");
    private static final Pattern COMPANY_PATTERNS = Pattern.compile(
            "(?:现任职于|目前任职于|现任职[:：]|目前任职[:：]|当前公司[:：]|现公司[:：])\\s*([\\u4e00-\\u9fa5A-Za-z0-9（）()·\\.\\s]{2,50})");
    private static final Pattern POSITION_PATTERN = Pattern.compile(
            "(?:现任职位|当前职位|应聘职位|职位[:：]|岗位[:：])\\s*([\\u4e00-\\u9fa5A-Za-z0-9/]{2,50})");

    @Value("${lin.file.store-dir:assets/}")
    private String storeDir;

    /**
     * 判断扩展名是否支持解析
     */
    public boolean supported(String filename) {
        String ext = extOf(filename);
        return ext.equals("pdf") || ext.equals("doc") || ext.equals("docx");
    }

    /**
     * 提取简历文本
     */
    public String extractText(MultipartFile file) {
        String ext = extOf(file.getOriginalFilename());
        try (InputStream in = file.getInputStream()) {
            switch (ext) {
                case "pdf":
                    try (PDDocument doc = PDDocument.load(in)) {
                        PDFTextStripper stripper = new PDFTextStripper();
                        return stripper.getText(doc);
                    }
                case "doc":
                    try (HWPFDocument doc = new HWPFDocument(in)) {
                        WordExtractor extractor = new WordExtractor(doc);
                        return extractor.getText();
                    }
                case "docx":
                    try (XWPFDocument doc = new XWPFDocument(in)) {
                        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
                        return extractor.getText();
                    }
                default:
                    throw new ForbiddenException(80200);
            }
        } catch (ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.error("简历解析失败: {}", file.getOriginalFilename(), e);
            throw new ForbiddenException(80190);
        }
    }

    /**
     * 规则解析文本为候选人草稿（字段缺失留空，由顾问补录）
     */
    public CandidateDO parseText(String text, String fileName) {
        CandidateDO candidate = CandidateDO.builder().source("sourcing").build();
        if (text == null || text.isEmpty()) {
            candidate.setName(nameFromFileName(fileName));
            return candidate;
        }
        String phone = findFirst(PHONE_PATTERN, text);
        String email = findFirst(EMAIL_PATTERN, text);
        candidate.setPhone(phone);
        candidate.setEmail(email);
        candidate.setName(nameFromTextOrFile(text, fileName));
        candidate.setEducation(educationFromText(text));
        candidate.setCurrentCompany(trim(findFirstGroup(COMPANY_PATTERNS, text), 100));
        candidate.setCurrentPosition(trim(findFirstGroup(POSITION_PATTERN, text), 100));
        return candidate;
    }

    /**
     * 保存简历原件到 assets/resume/yyyyMMdd/，返回相对路径（正斜杠）
     */
    public String saveResumeFile(MultipartFile file) {
        String ext = extOf(file.getOriginalFilename());
        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        try {
            Path dir = Paths.get(storeDir, "resume", dateDir);
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
            return "resume/" + dateDir + "/" + filename;
        } catch (Exception e) {
            log.error("简历文件保存失败: {}", file.getOriginalFilename(), e);
            throw new ForbiddenException(80190);
        }
    }

    private String findFirst(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group() : null;
    }

    private String findFirstGroup(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private String nameFromTextOrFile(String text, String fileName) {
        Matcher m = NAME_LABEL_PATTERN.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return nameFromFileName(fileName);
    }

    /**
     * 从文件名推断姓名：张三_Java工程师_简历.pdf -> 张三
     */
    private String nameFromFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        String base = fileName.replaceAll("\\.[^.]+$", "");
        String[] parts = base.split("[_\\-\\s()（）,，、]+");
        for (String part : parts) {
            if (part.length() >= 2 && part.length() <= 4 && part.matches("[\\u4e00-\\u9fa5]+")) {
                return part;
            }
        }
        return null;
    }

    /**
     * 学历关键词匹配（取最高学历）
     */
    private String educationFromText(String text) {
        if (text.contains("博士")) {
            return "doctor";
        }
        if (text.contains("硕士") || text.contains("研究生")) {
            return "master";
        }
        if (text.contains("本科") || text.contains("学士")) {
            return "bachelor";
        }
        if (text.contains("大专") || text.contains("专科")) {
            return "associate";
        }
        return null;
    }

    private String trim(String value, int max) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.length() > max ? v.substring(0, max) : v;
    }

    private String extOf(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
