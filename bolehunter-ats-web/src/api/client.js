import { get, post, put, _delete } from './request'

/**
 * 客户 CRM
 */
export const getClientPage = params => get('recruit/client/page', params)
export const getClientDetail = id => get(`recruit/client/${id}/detail`)
export const createClient = data => post('recruit/client', data)
export const updateClient = (id, data) => put(`recruit/client/${id}`, data)
export const assignOwner = (id, ownerId) => put(`recruit/client/${id}/owner`, { owner_id: ownerId })
export const changeClientStatus = (id, clientStatus) =>
  put(`recruit/client/${id}/status`, {}, { params: { client_status: clientStatus } })
export const getClientOwners = () => get('recruit/client/owners')
export const getClientOptions = () => get('recruit/client/options')
export const getContacts = companyId => get(`recruit/client/${companyId}/contact`)
export const createContact = (companyId, data) => post(`recruit/client/${companyId}/contact`, data)
export const updateContact = (companyId, contactId, data) =>
  put(`recruit/client/${companyId}/contact/${contactId}`, data)
export const deleteContact = (companyId, contactId) =>
  _delete(`recruit/client/${companyId}/contact/${contactId}`)
