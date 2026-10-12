#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
BoleHunter 伯乐猎 —— License 版权头合规检查
检查 *.java / *.vue 源文件头部是否包含 AGPL-3.0 版权声明（ZhuangRenyang）。
用法：python scripts/check_license.py [仓库根目录]
退出码：0=全部合规，1=存在缺失（CI 中断构建）
"""
import sys
from pathlib import Path

MARKERS = ("ZhuangRenyang",)          # 版权头必备标识
HEAD_LINES = 12                        # 只检查文件头部 N 行
EXTENSIONS = {".java", ".vue"}

# 生成代码与第三方目录豁免
EXEMPT_DIRS = {"node_modules", "dist", "target", ".git", ".idea", "gen-templates"}


def is_exempt(path: Path) -> bool:
    return any(part in EXEMPT_DIRS for part in path.parts)


def check_file(path: Path) -> bool:
    try:
        with open(path, "r", encoding="utf-8", errors="replace") as f:
            head = "".join(f.readline() for _ in range(HEAD_LINES))
        return all(marker in head for marker in MARKERS)
    except OSError:
        return False


def main() -> int:
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
    missing = []
    for ext in EXTENSIONS:
        for path in root.rglob(f"*{ext}"):
            if is_exempt(path.relative_to(root)):
                continue
            if not check_file(path):
                missing.append(path.relative_to(root).as_posix())
    if missing:
        print(f"❌ 版权头缺失 {len(missing)} 个文件（前 50 个）：")
        for item in missing[:50]:
            print(f"   {item}")
        print("请在文件头部补充 AGPL-3.0 版权声明，参考已有源文件。")
        return 1
    total = sum(1 for ext in EXTENSIONS for _ in root.rglob(f"*{ext}"))
    print(f"✅ 版权头合规检查通过（共扫描 {total} 个源文件）")
    return 0


if __name__ == "__main__":
    sys.exit(main())
