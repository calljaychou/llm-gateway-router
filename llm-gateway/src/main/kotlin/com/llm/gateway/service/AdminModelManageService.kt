package com.llm.gateway.service

import com.github.pagehelper.PageInfo
import com.github.pagehelper.page.PageMethod
import com.llm.gateway.billing.TokenChargeItem
import com.llm.gateway.common.enums.BillingType
import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.common.logger
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsDynamicSqlSupport
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsMapper
import com.llm.gateway.dal.mapper.MasterKeysMapper
import com.llm.gateway.dal.mapper.MasterKeysDynamicSqlSupport
import com.llm.gateway.dal.mapper.ModelPriceRuleDynamicSqlSupport
import com.llm.gateway.dal.mapper.ModelPriceRuleMapper
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport
import com.llm.gateway.dal.mapper.ModelsMapper
import com.llm.gateway.dal.mapper.VendorsDynamicSqlSupport
import com.llm.gateway.dal.mapper.VendorsMapper
import com.llm.gateway.dal.mapper.count
import com.llm.gateway.dal.mapper.deleteByPrimaryKey
import com.llm.gateway.dal.mapper.insert
import com.llm.gateway.dal.mapper.select
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.mapper.updateByPrimaryKeySelective
import com.llm.gateway.dal.model.MasterKeysRecord
import com.llm.gateway.dal.model.ModelPriceRuleRecord
import com.llm.gateway.dal.model.ModelsRecord
import com.llm.gateway.dal.model.VendorsRecord
import com.llm.gateway.model.PageResult
import com.llm.gateway.model.params.MasterKeyCreateParams
import com.llm.gateway.model.params.MasterKeyPageParams
import com.llm.gateway.model.params.ModelCreateParams
import com.llm.gateway.model.params.ModelPriceRuleCreateParams
import com.llm.gateway.model.params.ModelPriceRuleListParams
import com.llm.gateway.model.params.ModelPriceRuleUpdateParams
import com.llm.gateway.model.params.ModelUpdateParams
import com.llm.gateway.model.params.VendorCreateParams
import com.llm.gateway.model.results.MasterKeyCreateResult
import com.llm.gateway.model.results.MasterKeyListItemResult
import com.llm.gateway.model.results.MasterKeyListResult
import com.llm.gateway.model.results.MasterKeyPageItemResult
import com.llm.gateway.model.results.ModelCreateResult
import com.llm.gateway.model.results.ModelDeleteResult
import com.llm.gateway.model.results.ModelPriceRuleListItemResult
import com.llm.gateway.model.results.ModelUpdateResult
import com.llm.gateway.model.results.ModelVendorListItemResult
import com.llm.gateway.model.results.VendorCreateResult
import com.llm.gateway.model.results.VendorListItemResult
import java.net.URI
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Date
import javax.annotation.PostConstruct
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminModelManageService(
    private val vendorsMapper: VendorsMapper,
    private val masterKeysMapper: MasterKeysMapper,
    private val modelsMapper: ModelsMapper,
    private val modelPriceRuleMapper: ModelPriceRuleMapper,
    private val departmentModelPermissionsMapper: DepartmentModelPermissionsMapper,
    @Value("\${gateway.aes-secret}") private val aesSecret: String,
) {
    private val log = logger()

    companion object {
        private val ZERO_AMOUNT = BigDecimal.ZERO
    }

    @PostConstruct
    fun validateAesSecret() {
        if (aesSecret.isBlank() || aesSecret.length < 32) {
            throw IllegalStateException("gateway.aes-secret 未配置或长度不足(至少32字符)")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun createVendor(params: VendorCreateParams): VendorCreateResult {
        val existed = vendorsMapper.selectOne {
            where { VendorsDynamicSqlSupport.Vendors.name isEqualTo params.name.trim() }
        }
        if (existed != null) {
            throw BizException(BizException.BUSINESS_FAILED, "供应商名称已存在")
        }

        val now = Date()
        val normalizedBaseUrl = params.baseUrl.trim().trimEnd('/')
        if (!isValidBaseUrl(normalizedBaseUrl)) {
            throw BizException(BizException.BUSINESS_FAILED, "供应商Base URL不合法")
        }
        val record = VendorsRecord(
            name = params.name.trim(),
            baseUrl = normalizedBaseUrl,
            status = params.status ?: NormalStatus,
            createdTime = now,
            updatedTime = now,
        )
        try {
            vendorsMapper.insert(record)
        } catch (e: DataIntegrityViolationException) {
            throw mapDataIntegrityException(e, "供应商名称或URL重复")
        }
        val vendorId = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "创建供应商失败")

        return VendorCreateResult(
            vendorId = vendorId,
            name = record.name!!,
            baseUrl = record.baseUrl!!,
            status = record.status!!,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun createMasterKey(params: MasterKeyCreateParams): MasterKeyCreateResult {
        val vendorId = params.vendorId ?: throw BizException(BizException.BUSINESS_FAILED, "供应商ID不能为空")
        val vendor = vendorsMapper.selectOne {
            where { VendorsDynamicSqlSupport.Vendors.id isEqualTo vendorId }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "供应商不存在")

        if (vendor.status != NormalStatus) {
            throw BizException(BizException.BUSINESS_FAILED, "供应商状态异常，无法新增主密钥")
        }

        val now = Date()
        val record = MasterKeysRecord(
            vendorId = vendorId,
            weight = params.weight ?: 10,
            status = params.status ?: NormalStatus,
            errorCount = 0,
            lastCheckedAt = null,
            createdTime = now,
            updatedTime = now,
            apiKeyEncrypted = encryptApiKey(params.apiKey.trim()),
        )
        try {
            masterKeysMapper.insert(record)
        } catch (e: DataIntegrityViolationException) {
            throw mapDataIntegrityException(e, "主密钥创建失败，请检查数据是否重复")
        }
        val masterKeyId = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "创建主密钥失败")

        return MasterKeyCreateResult(
            masterKeyId = masterKeyId,
            vendorId = vendorId,
            status = record.status!!,
            weight = record.weight!!,
            keyPrefix = buildKeyPrefix(params.apiKey),
        )
    }

    fun listMasterKeys(params: MasterKeyPageParams): PageResult<MasterKeyPageItemResult> {
        PageMethod.startPage<MasterKeysRecord>(params.pageNum, params.pageSize)
        val records = masterKeysMapper.select {
            where { MasterKeysDynamicSqlSupport.MasterKeys.vendorId isEqualToWhenPresent params.vendorId }
            and { MasterKeysDynamicSqlSupport.MasterKeys.status isEqualToWhenPresent params.status }
            orderBy(
                MasterKeysDynamicSqlSupport.MasterKeys.vendorId,
                MasterKeysDynamicSqlSupport.MasterKeys.id.descending()
            )
        }
        val pageInfo = PageInfo.of(records)
        val vendorNames = listVendorNames(records)

        return PageResult(
            pageNum = pageInfo.pageNum,
            pageSize = pageInfo.pageSize,
            total = pageInfo.total,
            list = records.map { mapMasterKeyPageItem(it, vendorNames) },
        )
    }

    fun listMasterKeys(vendorId: Long?): MasterKeyListResult {
        val records = masterKeysMapper.select {
            where { MasterKeysDynamicSqlSupport.MasterKeys.vendorId isEqualToWhenPresent vendorId }
            orderBy(
                MasterKeysDynamicSqlSupport.MasterKeys.vendorId,
                MasterKeysDynamicSqlSupport.MasterKeys.id.descending()
            )
        }
        val vendorNames = listVendorNames(records)

        return MasterKeyListResult(
            vendorId = vendorId,
            keys = records.map { mapMasterKeyListItem(it, vendorNames) },
        )
    }

    fun listVendors(): List<VendorListItemResult> {
        val records = vendorsMapper.select {
            orderBy(VendorsDynamicSqlSupport.Vendors.id.descending())
        }
        return records.map { mapVendorListItem(it) }
    }

    fun listModelVendors(): List<ModelVendorListItemResult> {
        val models = modelsMapper.select {
            orderBy(ModelsDynamicSqlSupport.Models.id.descending())
        }
        if (models.isEmpty()) return emptyList()

        val vendors = vendorsMapper.select {
            orderBy(VendorsDynamicSqlSupport.Vendors.id.descending())
        }.mapNotNull { vendor ->
            vendor.id?.let { vendorId -> vendorId to vendor }
        }.toMap()

        return models.map { mapModelVendorListItem(it, vendors) }
    }

    fun listModelPriceRules(params: ModelPriceRuleListParams): List<ModelPriceRuleListItemResult> {
        val chargeItem = params.chargeItem?.trim()?.ifBlank { null }
        val currency = params.currency?.trim()?.ifBlank { null }
        val records = modelPriceRuleMapper.select {
            where { ModelPriceRuleDynamicSqlSupport.ModelPriceRule.modelId isEqualToWhenPresent params.modelId }
            and { ModelPriceRuleDynamicSqlSupport.ModelPriceRule.vendorId isEqualToWhenPresent params.vendorId }
            and { ModelPriceRuleDynamicSqlSupport.ModelPriceRule.chargeItem isEqualToWhenPresent chargeItem }
            and { ModelPriceRuleDynamicSqlSupport.ModelPriceRule.currency isEqualToWhenPresent currency }
            and { ModelPriceRuleDynamicSqlSupport.ModelPriceRule.active isEqualToWhenPresent params.active }
            orderBy(
                ModelPriceRuleDynamicSqlSupport.ModelPriceRule.modelId,
                ModelPriceRuleDynamicSqlSupport.ModelPriceRule.id.descending()
            )
        }
        return records.map { mapModelPriceRuleListItem(it) }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun createModelPriceRule(params: ModelPriceRuleCreateParams): ModelPriceRuleListItemResult {
        val modelId = params.modelId ?: throw BizException(BizException.BUSINESS_FAILED, "模型ID不能为空")
        val model = ensureModelExists(modelId)
        val vendorId = model.vendorId ?: throw BizException(BizException.SYSTEM_FAILED, "模型供应商ID异常")
        val chargeItem = validateModelPriceRuleChargeItem(params.chargeItem)
        val currency = params.currency?.trim()?.ifBlank { "CNY" } ?: "CNY"

        val existedRule = modelPriceRuleMapper.selectOne {
            where { ModelPriceRuleDynamicSqlSupport.ModelPriceRule.modelId isEqualTo modelId }
            and { ModelPriceRuleDynamicSqlSupport.ModelPriceRule.chargeItem isEqualTo chargeItem }
        }
        if (existedRule != null) {
            throw BizException(BizException.BUSINESS_FAILED, "模型计费项已存在")
        }

        val now = Date()
        val record = ModelPriceRuleRecord(
            modelId = modelId,
            vendorId = vendorId,
            chargeItem = chargeItem,
            priceCnyPerMillion = params.priceCnyPerMillion ?: ZERO_AMOUNT,
            currency = currency,
            active = params.active ?: true,
            createdAt = now,
            updatedAt = now,
        )
        try {
            modelPriceRuleMapper.insert(record)
        } catch (e: DataIntegrityViolationException) {
            throw mapDataIntegrityException(e, "模型计费项已存在")
        }

        log.info(
            "新增模型计费规则成功 ruleId={}, modelId={}, vendorId={}, chargeItem={}, priceCnyPerMillion={}, currency={}, active={}",
            record.id,
            record.modelId,
            record.vendorId,
            record.chargeItem,
            record.priceCnyPerMillion,
            record.currency,
            record.active
        )
        return mapModelPriceRuleListItem(record)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateModelPriceRule(ruleId: Long, params: ModelPriceRuleUpdateParams): ModelPriceRuleListItemResult {
        val existedRule = ensureModelPriceRuleExists(ruleId)
        val currency = params.currency?.trim()?.ifBlank {
            throw BizException(BizException.BUSINESS_FAILED, "币种不能为空")
        }
        val record = ModelPriceRuleRecord(
            id = ruleId,
            priceCnyPerMillion = params.priceCnyPerMillion ?: existedRule.priceCnyPerMillion,
            currency = currency ?: existedRule.currency,
            active = params.active ?: existedRule.active,
            updatedAt = Date(),
        )
        val updatedCount = modelPriceRuleMapper.updateByPrimaryKeySelective(record)
        if (updatedCount <= 0) {
            throw BizException(BizException.SYSTEM_FAILED, "编辑模型计费规则失败")
        }

        val updatedRule = ensureModelPriceRuleExists(ruleId)
        log.info(
            "编辑模型计费规则成功 ruleId={}, modelId={}, vendorId={}, chargeItem={}, priceCnyPerMillion={}, currency={}, active={}",
            ruleId,
            updatedRule.modelId,
            updatedRule.vendorId,
            updatedRule.chargeItem,
            updatedRule.priceCnyPerMillion,
            updatedRule.currency,
            updatedRule.active
        )
        return mapModelPriceRuleListItem(updatedRule)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun createModel(params: ModelCreateParams): ModelCreateResult {
        val vendorId = params.vendorId ?: throw BizException(BizException.BUSINESS_FAILED, "供应商ID不能为空")
        val vendor = vendorsMapper.selectOne {
            where { VendorsDynamicSqlSupport.Vendors.id isEqualTo vendorId }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "供应商不存在")

        if (vendor.status != NormalStatus) {
            throw BizException(BizException.BUSINESS_FAILED, "供应商状态异常，无法新增模型")
        }

        val alias = params.modelAlias.trim()
        val existedModel = modelsMapper.selectOne {
            where { ModelsDynamicSqlSupport.Models.modelAlias isEqualTo alias }
        }
        if (existedModel != null) {
            throw BizException(BizException.BUSINESS_FAILED, "模型别名已存在")
        }

        val now = Date()
        val billingType = (params.billingType ?: BillingType.PAID).value

        val record = ModelsRecord(
            modelAlias = alias,
            realModelName = params.realModelName.trim(),
            vendorId = vendorId,
            billingType = billingType,
            inputPriceCnyPerMillion = params.inputPriceCnyPerMillion ?: ZERO_AMOUNT,
            outputPriceCnyPerMillion = params.outputPriceCnyPerMillion ?: ZERO_AMOUNT,
            active = params.active ?: true,
            createdTime = now,
            updatedTime = now,
        )
        try {
            modelsMapper.insert(record)
        } catch (e: DataIntegrityViolationException) {
            throw mapDataIntegrityException(e, "模型别名重复")
        }
        val modelId = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "创建模型失败")

        return ModelCreateResult(
            modelId = modelId,
            modelAlias = record.modelAlias!!,
            realModelName = record.realModelName!!,
            vendorId = record.vendorId!!,
            billingType = record.billingType!!,
            inputPriceCnyPerMillion = record.inputPriceCnyPerMillion ?: ZERO_AMOUNT,
            outputPriceCnyPerMillion = record.outputPriceCnyPerMillion ?: ZERO_AMOUNT,
            active = record.active ?: true,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateModel(modelId: Long, params: ModelUpdateParams): ModelUpdateResult {
        val existedModel = ensureModelExists(modelId)
        val targetVendorId = params.vendorId ?: existedModel.vendorId
        if (targetVendorId == null) {
            throw BizException(BizException.SYSTEM_FAILED, "模型供应商ID异常")
        }
        if (params.vendorId != null) {
            ensureActiveVendor(params.vendorId)
        }

        val targetAlias = params.modelAlias?.trim() ?: existedModel.modelAlias.orEmpty()
        if (targetAlias.isBlank()) {
            throw BizException(BizException.BUSINESS_FAILED, "模型别名不能为空")
        }
        checkModelAliasUnique(modelId, targetAlias)

        val targetRealModelName = params.realModelName?.trim() ?: existedModel.realModelName.orEmpty()
        if (targetRealModelName.isBlank()) {
            throw BizException(BizException.BUSINESS_FAILED, "真实模型名不能为空")
        }

        val record = ModelsRecord(
            id = modelId,
            modelAlias = targetAlias,
            realModelName = targetRealModelName,
            vendorId = targetVendorId,
            billingType = params.billingType?.value ?: existedModel.billingType,
            active = params.active ?: existedModel.active,
            updatedTime = Date(),
        )
        try {
            modelsMapper.updateByPrimaryKeySelective(record)
        } catch (e: DataIntegrityViolationException) {
            throw mapDataIntegrityException(e, "模型别名重复")
        }

        val updatedModel = ensureModelExists(modelId)
        log.info("编辑模型成功 modelId={}, modelAlias={}, vendorId={}", modelId, updatedModel.modelAlias, updatedModel.vendorId)
        return ModelUpdateResult(
            modelId = modelId,
            modelAlias = updatedModel.modelAlias.orEmpty(),
            realModelName = updatedModel.realModelName.orEmpty(),
            vendorId = updatedModel.vendorId ?: throw BizException(BizException.SYSTEM_FAILED, "模型供应商ID异常"),
            billingType = updatedModel.billingType.orEmpty(),
            active = updatedModel.active ?: false,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun deleteModel(modelId: Long): ModelDeleteResult {
        ensureModelExists(modelId)
        checkModelHasNoActivePermission(modelId)

        val deletedCount = try {
            modelsMapper.deleteByPrimaryKey(modelId)
        } catch (_: DataIntegrityViolationException) {
            throw BizException(BizException.BUSINESS_FAILED, "模型已被业务数据引用，无法删除")
        }
        if (deletedCount <= 0) {
            throw BizException(BizException.SYSTEM_FAILED, "删除模型失败")
        }
        log.info("删除模型成功 modelId={}", modelId)

        return ModelDeleteResult(
            modelId = modelId,
            deleted = true,
        )
    }

    /**
     * 转换供应商列表项。
     */
    private fun mapVendorListItem(record: VendorsRecord): VendorListItemResult {
        return VendorListItemResult(
            id = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "供应商ID异常"),
            name = record.name.orEmpty(),
            baseUrl = record.baseUrl.orEmpty(),
            status = record.status ?: 0,
            createdTime = record.createdTime,
        )
    }

    /**
     * 转换模型供应商列表项，补充模型所属供应商名称。
     */
    private fun mapModelVendorListItem(
        record: ModelsRecord,
        vendors: Map<Long, VendorsRecord>,
    ): ModelVendorListItemResult {
        val vendorId = record.vendorId ?: throw BizException(BizException.SYSTEM_FAILED, "模型供应商ID异常")
        val vendorName = vendors[vendorId]?.name ?: "未知供应商"
        return ModelVendorListItemResult(
            id = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "模型ID异常"),
            modelAlias = record.modelAlias.orEmpty(),
            realModelName = record.realModelName.orEmpty(),
            vendorId = vendorId,
            vendorName = vendorName,
            billingType = record.billingType.orEmpty(),
            active = record.active ?: false,
            createdTime = record.createdTime,
        )
    }

    /**
     * 转换模型计费规则列表项。
     */
    private fun mapModelPriceRuleListItem(record: ModelPriceRuleRecord): ModelPriceRuleListItemResult {
        return ModelPriceRuleListItemResult(
            id = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "计费规则ID异常"),
            modelId = record.modelId ?: throw BizException(BizException.SYSTEM_FAILED, "模型ID异常"),
            vendorId = record.vendorId ?: throw BizException(BizException.SYSTEM_FAILED, "供应商ID异常"),
            chargeItem = record.chargeItem.orEmpty(),
            priceCnyPerMillion = record.priceCnyPerMillion ?: ZERO_AMOUNT,
            currency = record.currency.orEmpty(),
            active = record.active ?: false,
            createdAt = record.createdAt,
            updatedAt = record.updatedAt,
        )
    }

    /**
     * 校验供应商存在。
     */
    private fun ensureVendorExists(vendorId: Long): VendorsRecord {
        return vendorsMapper.selectOne {
            where { VendorsDynamicSqlSupport.Vendors.id isEqualTo vendorId }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "供应商不存在")
    }

    /**
     * 校验供应商存在且状态正常。
     */
    private fun ensureActiveVendor(vendorId: Long): VendorsRecord {
        val vendor = ensureVendorExists(vendorId)
        if (vendor.status != NormalStatus) {
            throw BizException(BizException.BUSINESS_FAILED, "供应商状态异常，无法关联模型")
        }
        return vendor
    }

    /**
     * 校验模型存在。
     */
    private fun ensureModelExists(modelId: Long): ModelsRecord {
        return modelsMapper.selectOne {
            where { ModelsDynamicSqlSupport.Models.id isEqualTo modelId }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "模型不存在")
    }

    /**
     * 校验模型计费规则存在。
     */
    private fun ensureModelPriceRuleExists(ruleId: Long): ModelPriceRuleRecord {
        return modelPriceRuleMapper.selectOne {
            where { ModelPriceRuleDynamicSqlSupport.ModelPriceRule.id isEqualTo ruleId }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "模型计费规则不存在")
    }

    /**
     * 校验模型计费项枚举合法。
     */
    private fun validateModelPriceRuleChargeItem(chargeItem: String): String {
        val normalizedChargeItem = chargeItem.trim()
        val matchedChargeItem = TokenChargeItem.values().firstOrNull { it.value == normalizedChargeItem }
        if (matchedChargeItem == null) {
            throw BizException(BizException.BUSINESS_FAILED, "计费项不合法")
        }
        return matchedChargeItem.value
    }

    /**
     * 校验模型别名唯一，编辑当前模型时排除自身。
     */
    private fun checkModelAliasUnique(modelId: Long, modelAlias: String) {
        val existed = modelsMapper.selectOne {
            where { ModelsDynamicSqlSupport.Models.modelAlias isEqualTo modelAlias }
        }
        if (existed != null && existed.id != modelId) {
            throw BizException(BizException.BUSINESS_FAILED, "模型别名已存在")
        }
    }

    /**
     * 删除模型前校验不存在启用中的部门模型权限。
     */
    private fun checkModelHasNoActivePermission(modelId: Long) {
        val permissionCount = departmentModelPermissionsMapper.count {
            where { DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.modelId isEqualTo modelId }
            and { DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.status isEqualTo NormalStatus }
        }
        if (permissionCount > 0L) {
            throw BizException(BizException.BUSINESS_FAILED, "模型已被部门权限引用，无法删除")
        }
    }

    /**
     * 转换主密钥列表项，不返回密钥明文或可逆密文。
     */
    private fun mapMasterKeyListItem(
        record: MasterKeysRecord,
        vendorNames: Map<Long, String>,
    ): MasterKeyListItemResult {
        val masterKeyId = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "主密钥ID异常")
        val vendorId = record.vendorId ?: throw BizException(BizException.SYSTEM_FAILED, "供应商ID异常")
        return MasterKeyListItemResult(
            masterKeyId = masterKeyId,
            vendorId = vendorId,
            vendorName = vendorNames[vendorId] ?: "",
            keyFingerprint = buildEncryptedKeyFingerprint(record.apiKeyEncrypted),
            weight = record.weight ?: 0,
            status = record.status ?: 0,
            errorCount = record.errorCount ?: 0,
            lastCheckedAt = record.lastCheckedAt,
            createdTime = record.createdTime,
            updatedTime = record.updatedTime,
        )
    }

    /**
     * 转换主密钥分页列表项，不返回密钥明文或可逆密文。
     */
    private fun mapMasterKeyPageItem(
        record: MasterKeysRecord,
        vendorNames: Map<Long, String>,
    ): MasterKeyPageItemResult {
        val vendorId = record.vendorId ?: throw BizException(BizException.SYSTEM_FAILED, "供应商ID异常")
        return MasterKeyPageItemResult(
            vendorId = vendorId,
            vendorName = vendorNames[vendorId] ?: "",
            keyInfo = buildEncryptedKeyFingerprint(record.apiKeyEncrypted),
            weight = record.weight ?: 0,
            status = record.status ?: 0,
            lastCheckedAt = record.lastCheckedAt,
            createdTime = record.createdTime,
        )
    }

    /**
     * 批量查询主密钥关联的供应商名称，避免列表映射时逐条访问数据库。
     */
    private fun listVendorNames(records: List<MasterKeysRecord>): Map<Long, String> {
        val vendorIds = records.mapNotNull { it.vendorId }.distinct()
        if (vendorIds.isEmpty()) return emptyMap()

        return vendorsMapper.select {
            where { VendorsDynamicSqlSupport.Vendors.id isIn vendorIds }
        }.associate { it.id!! to it.name!! }
    }

    /**
     * 基于加密后的主密钥生成不可逆短指纹，仅用于列表区分。
     */
    private fun buildEncryptedKeyFingerprint(apiKeyEncrypted: String?): String {
        if (apiKeyEncrypted.isNullOrBlank()) return "key-unknown"
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(apiKeyEncrypted.toByteArray(StandardCharsets.UTF_8))
        val fingerprint = Base64.getUrlEncoder().withoutPadding().encodeToString(digest).take(12)
        return "key-$fingerprint"
    }

    private fun encryptApiKey(apiKey: String): String {
        if (apiKey.isBlank()) {
            throw BizException(BizException.BUSINESS_FAILED, "API Key不能为空")
        }
        val keySpec = buildAesKey()
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(128, iv))
        val encryptedBytes = cipher.doFinal(apiKey.toByteArray(StandardCharsets.UTF_8))
        val ivEncoded = Base64.getEncoder().encodeToString(iv)
        val dataEncoded = Base64.getEncoder().encodeToString(encryptedBytes)
        return "$ivEncoded:$dataEncoded"
    }

    private fun buildAesKey(): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(aesSecret.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(digest, "AES")
    }

    private fun buildKeyPrefix(apiKey: String): String {
        val value = apiKey.trim()
        if (value.length <= 8) {
            return "****"
        }
        return "${value.take(6)}****${value.takeLast(2)}"
    }

    private fun isValidBaseUrl(url: String): Boolean {
        if (url.isBlank()) return false
        return try {
            val uri = URI(url)
            val validScheme = uri.scheme == "http" || uri.scheme == "https"
            validScheme && !uri.host.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    private fun mapDataIntegrityException(e: DataIntegrityViolationException, duplicateMessage: String): BizException {
        val message = e.message.orEmpty().lowercase()
        return if (message.contains("duplicate") || message.contains("unique")) {
            BizException(BizException.BUSINESS_FAILED, duplicateMessage)
        } else {
            BizException(BizException.SYSTEM_FAILED, "数据写入失败，请检查输入内容")
        }
    }
}
