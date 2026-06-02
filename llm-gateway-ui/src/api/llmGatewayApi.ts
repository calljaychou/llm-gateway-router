import axios from 'axios';

// 创建 Axios 统一网关实例
const api = axios.create({
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// 1. 请求拦截器：自动注入 JWT 认证 Token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('llm_gateway_token');
        if (token && config.headers) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// 2. 响应拦截器：集中处理鉴权失败，实现未登录状态拦截
api.interceptors.response.use(
    (response) => response,
    (error) => {
        // 当后端抛出 401 未认证或 Token 过期，以及 403 无权限状态码时
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            localStorage.removeItem('llm_gateway_token');
            localStorage.removeItem('llm_gateway_username');

            // 触发全局自定义事件，通知最顶层的 App 组件切换回登录视图
            window.dispatchEvent(new Event('llm-gateway-unauthorized'));
        }
        return Promise.reject(error);
    }
);

// 通用后端响应体包装模型
export interface ApiResult<T> {
    code: number;
    data: T;
    message: string;
    success: boolean;
    timestamp: number;
}

// 通用分页数据包装模型
export interface PageResult<T> {
    list: T[];
    pageNum: number;
    pageSize: number;
    total: number;
}

/** ==================== 0. 认证相关类型与接口 ==================== */
export interface LoginRequest {
    username: string; // 用户名或邮箱
    password: string; // 密码
}

export interface LoginResult {
    email?: string;
    expiresInSeconds: number;
    passwordChanged: boolean;
    roles: string[];
    token: string;
    tokenType: string; // 例如: "Bearer"
    userId: number;
    username: string;
}

export const authApi = {
    // 登录并获取 JWT Token
    login: async (params: LoginRequest) => {
        const res = await api.post<ApiResult<LoginResult>>('/api/auth/login', params);
        return res.data;
    },
};

/** ==================== 1. 用户端-虚拟密钥管理类型与接口 ==================== */
export interface CreateVirtualKeyParams {
    name: string;
}

export interface CreateVirtualKeyResult {
    apiKey: string;
    keyId: number;
    keyPrefix: string;
    name: string;
}

export interface VirtualKeyListItem {
    expiresTime?: string;
    keyId: number;
    keyPrefix: string;
    name: string;
    status: number;
}

export interface VirtualKeyListResult {
    keys: VirtualKeyListItem[];
}

export interface RevokeVirtualKeyResult {
    keyId: number;
    revoked: boolean;
}

export const virtualKeyApi = {
    listKeys: async () => {
        const res = await api.get<ApiResult<VirtualKeyListResult>>('/admin/user/keys');
        return res.data;
    },
    createKey: async (params: CreateVirtualKeyParams) => {
        const res = await api.post<ApiResult<CreateVirtualKeyResult>>('/admin/user/keys', params);
        return res.data;
    },
    revokeKey: async (id: number) => {
        const res = await api.delete<ApiResult<RevokeVirtualKeyResult>>(`/admin/user/keys/${id}`);
        return res.data;
    }
};

/** ==================== 2. 用户端-配额仪表盘与流水类型 ==================== */
export interface UserQuotaBatch {
    consumedAmount: number;
    createdAt?: string;
    expiredAmount: number;
    expiresAt: string;
    grantId: number;
    grantedBy?: number;
    grantedAmount: number;
    remainingAmount: number;
    remark?: string;
    sourceGrantId?: number;
    sourceType: string;
    sourceUserId?: number;
    status: string;
    updatedAt?: string;
    userId: number;
}

export interface UserQuotaAccountSnapshot {
    activeGrants: UserQuotaBatch[];
    allowTransferOut: boolean;
    availableAmount: number;
    currentQuotaAmount: number;
    earliestExpireAt?: string;
    expiredAmount: number;
    transferredInAmount: number;
    transferredOutAmount: number;
    updatedAt?: string;
    usedAmount: number;
    userId: number;
}

export interface QuotaTransactionItem {
    availableAfterAmount: number;
    availableBeforeAmount: number;
    bizNo: string;
    changeType: string;
    counterpartyUserId?: number;
    createdAt: string;
    deltaAmount: number;
    grantId?: number;
    operatorUserId?: number;
    quotaAfterAmount: number;
    quotaBeforeAmount: number;
    remark?: string;
    requestId?: string;
    transactionId: number;
    userId: number;
}

export interface QuotaTransferParams {
    targetUser: string;
    transferAmount: number;
    remark?: string;
}

export interface QuotaTransferResult {
    fromAccount: UserQuotaAccountSnapshot;
    fromUserId: number;
    targetAccount: UserQuotaAccountSnapshot;
    targetUserId: number;
    transferAmount: number;
}

export const quotaApi = {
    getCurrentSnapshot: async () => {
        const res = await api.get<ApiResult<UserQuotaAccountSnapshot>>('/admin/user/quotas/current');
        return res.data;
    },
    listTransactions: async (pageNum: number, pageSize: number, type?: string) => {
        const res = await api.get<ApiResult<PageResult<QuotaTransactionItem>>>('/admin/user/quotas/transactions', {
            params: { pageNum, pageSize, type }
        });
        return res.data;
    },
    transferQuota: async (params: QuotaTransferParams) => {
        const res = await api.post<ApiResult<QuotaTransferResult>>('/admin/user/quotas/transfer', params);
        return res.data;
    }
};

/** ==================== 3. 管理端-组织架构与用户管理 ==================== */
export interface CreateDepartmentParams {
    deptName: string;
    leaderName?: string;
    orderNum?: number;
    tel?: string;
}

export interface DepartmentTreeItem {
    id: number;
    name: string;
    orderNum: number;
    parentId: number;
    status: number;
    children?: DepartmentTreeItem[];
    leaderName?: string;
    tel?: string;
}

export interface DepartmentPermissionItem {
    modelAlias: string;
    scope: 'SELF' | 'SUBTREE';
    status: number;
}

export interface ReplaceDepartmentPermissionsParams {
    items: DepartmentPermissionItem[];
}

export interface DepartmentPermissionViewItem {
    active: boolean;
    billingType: string;
    modelAlias: string;
    realModelName: string;
    scope: 'SELF' | 'SUBTREE';
    status: number;
    sourceDeptId: number;
    sourceDeptName: string;
    vendorId: number;
    vendorName: string;
}

export interface DepartmentPermissionsViewResult {
    deptId: number;
    models: DepartmentPermissionViewItem[];
}

export interface DepartmentPermissionsUpdateResult {
    deptId: number;
    added: number;
    removed: number;
    updated: number;
}

export interface CreateAdminUserParams {
    name: string;
    username: string;
    email: string;
    mobile?: string;
    gender?: number;
    status?: number;
    deptId: number;
    roleKeys: string[];
    password: string;
    forcePasswordChange?: boolean;
}

export interface UpdateAdminUserParams {
    name: string;
    username: string;
    email: string;
    mobile?: string;
    gender?: number;
    status?: number;
    deptId: number;
    roleKeys: string[];
}

export interface AdminUserCreateResult {
    userId: number;
    passwordChanged: boolean;
}

export interface AdminUserUpdateResult {
    userId: number;
    roleCount: number;
}

export interface ChangeAdminUserPasswordParams {
    password: string;
}

export interface AdminUserPasswordChangeResult {
    userId: number;
    passwordChanged: boolean;
}

export interface AdminUserQuotaAdjustmentParams {
    adjustAmount: number;
    expiresAt?: string;
    remark?: string;
}

export interface AdminUserQuotaTransferPermissionParams {
    allowTransferOut: boolean;
}

export interface AdminUserPageParams {
    deptId?: number;
    mobile?: string;
    email?: string;
    pageNum: number;
    pageSize: number;
}

export interface AdminUserListItem {
    userId: number;
    name?: string;
    username: string;
    mobile?: string;
    deptId?: number;
    deptName?: string;
    email?: string;
    gender: number;
    status: number;
    createdTime?: string;
}

export interface AdminUserBaseInfo {
    userId: number;
    name?: string;
    deptId?: number;
    username?: string;
    email?: string;
    mobile?: string;
    gender?: number;
    avatarUrl?: string;
    passwordChanged?: boolean;
    remark?: string;
    status?: number;
    delFlag?: boolean;
    createdTime?: string;
    updatedTime?: string;
}

export interface AdminUserDepartmentInfo {
    deptId: number;
    parentId: number;
    deptName: string;
    orderNum: number;
    leaderName?: string;
    tel?: string;
    status: number;
}

export interface AdminUserModelPermission {
    active: boolean;
    billingType: string;
    modelAlias: string;
    realModelName: string;
    scope: 'SELF' | 'SUBTREE';
    sourceDeptId: number;
    sourceDeptName: string;
    vendorId: number;
    vendorName: string;
}

export interface AdminUserQuotaConfig {
    userId: number;
    currentQuotaAmount: number;
    availableAmount: number;
    usedAmount: number;
    expiredAmount: number;
    transferredInAmount: number;
    transferredOutAmount: number;
    allowTransferOut: boolean;
    earliestExpireAt?: string;
    updatedAt?: string;
}

export interface AdminUserDetail {
    user: AdminUserBaseInfo;
    department?: AdminUserDepartmentInfo;
    roles: RoleListItem[];
    modelPermissions: AdminUserModelPermission[];
    quota?: AdminUserQuotaConfig;
}

export interface RoleListItem {
    id: number;
    roleName: string;
    roleKey: string;
    roleSort: number;
    createdBy?: string;
    createdTime?: string;
}

export interface CreateRoleParams {
    roleName: string;
    roleKey: string;
    roleSort?: number;
}

export interface RoleCreateResult {
    roleId: number;
    roleName: string;
    roleKey: string;
}

export interface RoleDeleteResult {
    roleId: number;
    deleted: boolean;
    removedUserRoleRelCount: number;
}

export const adminUserApi = {
    listUsers: async (params: AdminUserPageParams) => {
        const res = await api.get<ApiResult<PageResult<AdminUserListItem>>>('/admin/users', { params });
        return res.data;
    },
    getUserDetail: async (id: number) => {
        const res = await api.get<ApiResult<AdminUserDetail>>(`/admin/users/${id}/detail`);
        return res.data;
    },
    createUser: async (params: CreateAdminUserParams) => {
        const res = await api.post<ApiResult<AdminUserCreateResult>>('/admin/users', params);
        return res.data;
    },
    updateUser: async (id: number, params: UpdateAdminUserParams) => {
        const res = await api.put<ApiResult<AdminUserUpdateResult>>(`/admin/users/${id}`, params);
        return res.data;
    },
    changeUserPassword: async (id: number, params: ChangeAdminUserPasswordParams) => {
        const res = await api.put<ApiResult<AdminUserPasswordChangeResult>>(`/admin/users/${id}/password`, params);
        return res.data;
    },
    adjustUserQuota: async (id: number, params: AdminUserQuotaAdjustmentParams) => {
        const res = await api.post<ApiResult<UserQuotaAccountSnapshot>>(`/admin/users/${id}/quota/adjustments`, params);
        return res.data;
    },
    updateUserQuotaTransferPermission: async (id: number, params: AdminUserQuotaTransferPermissionParams) => {
        const res = await api.put<ApiResult<UserQuotaAccountSnapshot>>(`/admin/users/${id}/quota/transfer-permission`, params);
        return res.data;
    }
};

export const adminRoleApi = {
    listRoles: async () => {
        const res = await api.get<ApiResult<RoleListItem[]>>('/admin/roles');
        return res.data;
    },
    createRole: async (params: CreateRoleParams) => {
        const res = await api.post<ApiResult<RoleCreateResult>>('/admin/roles', params);
        return res.data;
    },
    deleteRole: async (id: number) => {
        const res = await api.delete<ApiResult<RoleDeleteResult>>(`/admin/roles/${id}`);
        return res.data;
    }
};

export const adminDeptApi = {
    // 获取整个组织的部门层级树
    getDeptTree: async () => {
        const res = await api.get<ApiResult<DepartmentTreeItem[]>>('/admin/departments/tree');
        return res.data;
    },
    // 创建根部门（一级部门）
    createRootDept: async (params: CreateDepartmentParams) => {
        const res = await api.post<ApiResult<any>>('/admin/departments', params);
        return res.data;
    },
    // 在指定父级下创建子部门
    createChildDept: async (parentId: number, params: CreateDepartmentParams) => {
        const res = await api.post<ApiResult<any>>(`/admin/departments/${parentId}/children`, params);
        return res.data;
    },
    // 获取部门的直接/有效模型使用权限
    getDeptPermissions: async (deptId: number, view: 'direct' | 'effective' = 'direct') => {
        const res = await api.get<ApiResult<DepartmentPermissionsViewResult>>(`/admin/departments/${deptId}/permissions`, { params: { view } });
        return res.data;
    },
    // 覆盖更新该部门的模型权限策略
    replaceDeptPermissions: async (deptId: number, params: ReplaceDepartmentPermissionsParams) => {
        const res = await api.put<ApiResult<DepartmentPermissionsUpdateResult>>(`/admin/departments/${deptId}/permissions`, params);
        return res.data;
    },
    // 删除部门
    deleteDept: async (id: number) => {
        const res = await api.delete<ApiResult<any>>(`/admin/departments/${id}`);
        return res.data;
    }
};

/** ==================== 4. 管理端-大模型与主密钥管理 ==================== */
export interface CreateVendorParams {
    baseUrl: string;
    name: string;
    status: number;
}

export interface VendorListItem {
    id: number;
    name: string;
    baseUrl: string;
    status: number;
    createdTime: string; // 假设后端返回创建时间
}

export interface CreateModelParams {
    active: boolean;
    billingType: 'FREE' | 'PAID';
    modelAlias: string;
    realModelName: string;
    vendorId: number;
}

export interface ModelVendorListItem {
    id: number;
    modelAlias: string;
    realModelName: string;
    billingType: string;
    active: boolean;
    vendorId: number;
    vendorName: string; // 关联的供应商名称
}

export interface ModelPriceRuleListParams {
    active?: boolean;
    chargeItem?: string;
    currency?: string;
    modelId?: number;
    vendorId?: number;
}

export interface ModelPriceRuleListItem {
    id: number;
    modelId: number;
    vendorId: number;
    chargeItem: string;
    priceCnyPerMillion: number;
    currency: string;
    active: boolean;
    createdAt?: string;
    updatedAt?: string;
}

export interface CreateModelPriceRuleParams {
    active?: boolean;
    chargeItem: string;
    currency?: string;
    modelId: number;
    priceCnyPerMillion: number;
}

export interface UpdateModelPriceRuleParams {
    active?: boolean;
    currency?: string;
    priceCnyPerMillion?: number;
}

export interface CreateMasterKeyParams {
    apiKey: string;
    status: number;
    vendorId: number;
    weight: number;
}

export interface MasterKeyPageParams {
    vendorId?: number;
    status?: number;
    pageNum: number;
    pageSize: number;
}

export interface MasterKeyListItem {
    vendorId: number;
    vendorName: string;
    keyInfo: string;
    weight: number;
    status: number;
    lastCheckedAt?: string;
    createdTime?: string;
}

export interface UpdateModelParams {
    active: boolean;
    billingType: 'FREE' | 'PAID';
    modelAlias: string;
    realModelName: string;
    vendorId: number;
}

export const adminGatewayApi = {
    // --- 供应商 (Vendor) 相关 ---
    listVendors: async () => {
        // 对应新补充的查询供应商列表 API
        const res = await api.get<ApiResult<VendorListItem[]>>('/admin/vendors');
        return res.data;
    },
    createVendor: async (params: CreateVendorParams) => {
        const res = await api.post<ApiResult<any>>('/admin/vendors', params);
        return res.data;
    },

    // --- 模型路由映射 (Model) 相关 ---
    listModelVendors: async () => {
        // 对应新补充的查询模型供应商列表 API
        const res = await api.get<ApiResult<ModelVendorListItem[]>>('/admin/models');
        return res.data;
    },
    listModelPriceRules: async (params?: ModelPriceRuleListParams) => {
        const res = await api.get<ApiResult<ModelPriceRuleListItem[]>>('/admin/model-price-rules', { params });
        return res.data;
    },
    createModelPriceRule: async (params: CreateModelPriceRuleParams) => {
        const res = await api.post<ApiResult<ModelPriceRuleListItem>>('/admin/model-price-rules', params);
        return res.data;
    },
    updateModelPriceRule: async (ruleId: number, params: UpdateModelPriceRuleParams) => {
        const res = await api.put<ApiResult<ModelPriceRuleListItem>>(`/admin/model-price-rules/${ruleId}`, params);
        return res.data;
    },
    createModel: async (params: CreateModelParams) => {
        const res = await api.post<ApiResult<any>>('/admin/models', params);
        return res.data;
    },

    // --- 主密钥 (Master Key) 相关 ---
    createMasterKey: async (params: CreateMasterKeyParams) => {
        const res = await api.post<ApiResult<any>>('/admin/master-keys', params);
        return res.data;
    },
    // 主密钥列表
    listMasterKeys: async (params: MasterKeyPageParams) => {
        const res = await api.get<ApiResult<PageResult<MasterKeyListItem>>>('/admin/master-keys', { params });
        return res.data;
    },
    // 编辑已有模型映射
    updateModel: async (modelId: number, params: UpdateModelParams) => {
        const res = await api.put<ApiResult<any>>(`/admin/models/${modelId}`, params);
        return res.data;
    },

    // 删除模型映射
    deleteModel: async (modelId: number) => {
        const res = await api.delete<ApiResult<any>>(`/admin/models/${modelId}`);
        return res.data;
    },
};
