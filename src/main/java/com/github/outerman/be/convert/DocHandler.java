package com.github.outerman.be.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanMap;

import com.github.outerman.be.BusinessDocEngine;
import com.github.outerman.be.model.Account;
import com.github.outerman.be.model.BusinessVoucher;
import com.github.outerman.be.model.BusinessVoucherDetail;
import com.github.outerman.be.model.BusinessVoucherSettle;
import com.github.outerman.be.model.Doc;
import com.github.outerman.be.model.DocEntry;
import com.github.outerman.be.model.DocTemplate;
import com.github.outerman.be.model.Org;
import com.github.outerman.be.model.SettleTemplate;
import com.github.outerman.be.template.BusinessTemplate;
import com.github.outerman.be.util.AmountGetter;
import com.github.outerman.be.util.DoubleUtil;
import com.github.outerman.be.util.StringUtil;
import org.apache.commons.lang3.BooleanUtils;

public class DocHandler {

    private Org org;

    private BusinessVoucher voucher;

    private Map<String, BusinessTemplate> templateMap;

    private Map<String, Account> accountMap;

    private Doc doc;

    /** 借方主科目分录 */
    private List<DocEntry> debitMainList = new ArrayList<>();
    /** 借方税科目分录 */
    private List<DocEntry> debitTaxList = new ArrayList<>();
    /** 贷方主科目分录 */
    private List<DocEntry> creditMainList = new ArrayList<>();
    /** 贷方税科目分录 */
    private List<DocEntry> creditTaxList = new ArrayList<>();

    /** 以合并标识为 key 的凭证分录 map */
    private Map<String, DocEntry> entryMap = new HashMap<>();;

    /** 排序的凭证分录 map */
    private Map<String, DocEntry> orderedEntryMap = new TreeMap<>();

    public DocHandler(Org org, BusinessVoucher voucher) {
        this.org = org;
        this.voucher = voucher;
        this.doc = getDefaultDoc(voucher);
    }

    /**
     * 根据单据信息获取默认的凭证 dto
     * @param voucher 单据信息
     */
    private Doc getDefaultDoc(BusinessVoucher voucher) {
        Doc doc = new Doc();
        doc.setSourceVoucherId(voucher.getSourceVoucherId());
        doc.setSourceVoucherCode(voucher.getSourceVoucherCode());
        doc.setSourceVoucherTypeId(voucher.getSourceVoucherTypeId());
        doc.setSourceVouchers(voucher.getSourceVouchers());
        doc.setAttachedVoucherNum(voucher.getAppendNum());
        doc.setVoucherDate(voucher.getVoucherDate());
        doc.setDocId(voucher.getDocId());
        doc.setCode(voucher.getDocCode());
        return doc;
    }

    public Doc getDoc() {
        List<DocEntry> entrys = new ArrayList<>();
        Boolean orderByFlag = voucher.getOrderByFlag();
        if (orderByFlag != null && orderByFlag) {
            entrys.addAll(orderedEntryMap.values());
        } else {
            entrys.addAll(debitMainList);
            entrys.addAll(debitTaxList);
            entrys.addAll(creditMainList);
            entrys.addAll(creditTaxList);
        }
        // 正负混录的业务明细，分录合并之后分录金额可能为 0 ，需要去掉金额为 0 的分录
        List<DocEntry> zeroAmountList = new ArrayList<>();
        for (DocEntry docEntry : entrys) {
            if (DoubleUtil.isNullOrZero(docEntry.getAmountDr()) && DoubleUtil.isNullOrZero(docEntry.getAmountCr())) {
                zeroAmountList.add(docEntry);
            }
        }
        if (!zeroAmountList.isEmpty()) {
            entrys.removeAll(zeroAmountList);
        }
        doc.setEntrys(entrys);
        return doc;
    }

    public void addEntry(DocTemplate docTemplate, BusinessVoucherDetail detail) {
        InnerFiDocEntryDto innerEntry = getDocEntryDto(docTemplate, detail);
        if (innerEntry == null) {
            return;
        }

        String key = innerEntry.getKey();
        DocEntry entry = innerEntry.getFiDocEntryDto();
        if (entryMap.containsKey(key)) {
            handleMerge(entry, key);
        } else {
            addEntry(entry, detail, null);
            entry.getSummaryList().add(entry.getSummary());
            entryMap.put(key, entry);
        }
        Boolean orderByFlag = voucher.getOrderByFlag();
        if (orderByFlag != null && orderByFlag) {
            orderedEntryMap.put(key, entryMap.get(key));
        }
    }

    private void handleMerge(DocEntry entry, String key) {
        if (!entryMap.containsKey(key)) {
            return;
        }
        // 按照分录合并规则需要合并
        Double quantity = entry.getQuantity();
        DocEntry existEntry = entryMap.get(key);
        if (quantity != null || existEntry.getQuantity() != null) {
            existEntry.setQuantity(DoubleUtil.addQuantity(quantity, existEntry.getQuantity()));
        }
        existEntry.setAmountCr(DoubleUtil.addAmount(existEntry.getAmountCr(), entry.getAmountCr()));
        existEntry.setOrigAmountCr(DoubleUtil.addAmount(existEntry.getOrigAmountCr(), entry.getOrigAmountCr()));
        existEntry.setAmountDr(DoubleUtil.addAmount(existEntry.getAmountDr(), entry.getAmountDr()));
        existEntry.setOrigAmountDr(DoubleUtil.addAmount(existEntry.getOrigAmountDr(), entry.getOrigAmountDr()));
        if (!DoubleUtil.isNullOrZero(existEntry.getAmountCr()) && !DoubleUtil.isNullOrZero(existEntry.getAmountDr())) {
            Double amountCr = DoubleUtil.addAmount(existEntry.getAmountCr(), -existEntry.getAmountDr());
            if (amountCr >= 0) {
                existEntry.setAmountCr(amountCr);
                existEntry.setOrigAmountCr(DoubleUtil.addAmount(existEntry.getOrigAmountCr(), -existEntry.getOrigAmountDr()));
                existEntry.setAmountDr(null);
                existEntry.setOrigAmountDr(null);
            } else {
                existEntry.setAmountDr(-amountCr);
                existEntry.setOrigAmountDr(DoubleUtil.addAmount(existEntry.getOrigAmountDr(), -existEntry.getOrigAmountCr()));
                existEntry.setAmountCr(null);
                existEntry.setOrigAmountCr(null);
            }
        }
        Double amount = existEntry.getAmountCr();
        if (DoubleUtil.isNullOrZero(amount)) {
            amount = existEntry.getAmountDr();
        }
        existEntry.setPrice(DoubleUtil.divPrice(amount, existEntry.getQuantity()));
        // 需要合并拼接摘要时，进行不同摘要的拼接
        if(entry.isMergeSummary() && !existEntry.getSummaryList().contains(entry.getSummary())){
            existEntry.getSummaryList().add(entry.getSummary());
            existEntry.setSummary(String.join("；", existEntry.getSummaryList()));
        }
    }

    private InnerFiDocEntryDto getDocEntryDto(DocTemplate docTemplate, BusinessVoucherDetail detail) {
        Map<String, Object> params = new HashMap<>();
        if (detail.getAmountMap() != null) {
            params.putAll(detail.getAmountMap());
        }
        if (detail.getInfluenceMap() != null) {
            params.putAll(detail.getInfluenceMap());
        }
        BeanMap map = new org.apache.commons.beanutils.BeanMap(org);
        for (Entry<Object, Object> entry : map.entrySet()) {
            params.put(entry.getKey().toString(), entry.getValue());
        }
        Double amount = AmountGetter.getAmount(detail, docTemplate.getAmountSource(), params);
        if (DoubleUtil.isNullOrZero(amount)) {
            return null;
        }

        Account account = docTemplate.getAccount();
        if (docTemplate.isLast()) {
            if (!DoubleUtil.isNullOrZero(docTemplate.getSum())) {
                amount = DoubleUtil.add(amount, - docTemplate.getSum());
            }
        } else {
            if (account.getDevelopRatio() != null) {
                amount = DoubleUtil.multi(amount, account.getDevelopRatio());
            }
            Double sum = DoubleUtil.add(docTemplate.getSum(), amount);
            docTemplate.setSum(sum);
        }
        if (DoubleUtil.isNullOrZero(amount)) {
            return null;
        }
        StringBuilder key = new StringBuilder(); // key 作为分录合并依据，同时单独排序时作为排序字段
        DocEntry entry = new DocEntry();
        Boolean mergeWithFlag = detail.getMergeWithFlag();
        Boolean orderByFlag = voucher.getOrderByFlag();
        if ((mergeWithFlag != null && mergeWithFlag) || (orderByFlag != null && orderByFlag)) {
            String flag = docTemplate.getFlag();
            if (!StringUtil.isEmpty(detail.getFlag())) {
                flag = detail.getFlag();
            }
            key.append("_flag" + flag);
        }
        entry.setSourceFlag(docTemplate.getFlag());
        String summary;
        if (!StringUtil.isEmpty(docTemplate.getSummary())) {
            summary = docTemplate.getSummary();
        } else {
            summary = detail.getMemo();
        }
        // 如果结算对方科目合并，则合并 key 不再考虑摘要
        if (!detail.getMerge()) {
            key.append("_summary").append(summary);
        }
        entry.setMergeSummary(detail.getMerge());
        entry.setSummary(summary);

        entry.setAccountId(account.getId());
        entry.setAccountCode(account.getCode());
        key.append("_accountCode").append(account.getCode());

        key.append("_businessType").append(detail.getBusinessType());
        entry.setSourceBusinessTypeId(detail.getBusinessType());
        // 0 借 1 贷，流水账不区分币种，本币原币金额一样
        Integer direction = docTemplate.getBalanceDirection();
        if (direction == null) {
            direction = 0;
        }
        // 处理分录借贷方向、正负金额是否需要颠倒
        if (detail.getReversal() && amount < 0) {
            direction = 1 - direction;
            amount*= -1;
        }
        if (direction != null && direction.equals(1)) {
            entry.setAmountCr(amount);
            entry.setOrigAmountCr(amount);
        } else {
            entry.setAmountDr(amount);
            entry.setOrigAmountDr(amount);
        }

        if (account.getIsAuxAccDepartment() != null && account.getIsAuxAccDepartment()) { // 部门
            entry.setDepartmentId(detail.getDepartment());
            key.append("_departmentId").append(detail.getDepartment());
        }
        if (account.getIsAuxAccPerson() != null && account.getIsAuxAccPerson()) { // 人员
            entry.setPersonId(detail.getEmployee());
            key.append("_personId").append(detail.getEmployee());
        }
        if (account.getIsAuxAccCustomer() != null && account.getIsAuxAccCustomer()) { // 客户
            entry.setCustomerId(detail.getConsumer());
            key.append("_customerId").append(detail.getConsumer());
        }
        if (account.getIsAuxAccSupplier() != null && account.getIsAuxAccSupplier()) { // 供应商
            entry.setSupplierId(detail.getVendor());
            key.append("_supplierId").append(detail.getVendor());
        }
        if (account.getIsAuxAccInventory() != null && account.getIsAuxAccInventory()) { // 存货
            entry.setInventoryId(detail.getInventory());
            key.append("_inventoryId").append(detail.getInventory());
        }
        if (account.getIsAuxAccProject() != null && account.getIsAuxAccProject()) { // 项目
            entry.setProjectId(detail.getProject());
            key.append("_projectId").append(detail.getProject());
        }
        // 自定义辅助核算
        if (account.getIsAuxAccExCalc1() != null && account.getIsAuxAccExCalc1()) {
            entry.setExCalc1(detail.getExCalc1Id());
            key.append("_exCalc1Id").append(detail.getExCalc1Id());
        }
        if (account.getIsAuxAccExCalc2() != null && account.getIsAuxAccExCalc2()) {
            entry.setExCalc2(detail.getExCalc2Id());
            key.append("_exCalc2Id").append(detail.getExCalc2Id());
        }
        if (account.getIsAuxAccExCalc3() != null && account.getIsAuxAccExCalc3()) {
            entry.setExCalc3(detail.getExCalc3Id());
            key.append("_exCalc3Id").append(detail.getExCalc3Id());
        }
        if (account.getIsAuxAccExCalc4() != null && account.getIsAuxAccExCalc4()) {
            entry.setExCalc4(detail.getExCalc4Id());
            key.append("_exCalc4Id").append(detail.getExCalc4Id());
        }
        if (account.getIsAuxAccExCalc5() != null && account.getIsAuxAccExCalc5()) {
            entry.setExCalc5(detail.getExCalc5Id());
            key.append("_exCalc5Id").append(detail.getExCalc5Id());
        }
        if (account.getIsAuxAccExCalc6() != null && account.getIsAuxAccExCalc6()) {
            entry.setExCalc6(detail.getExCalc6Id());
            key.append("_exCalc6Id").append(detail.getExCalc6Id());
        }
        if (account.getIsAuxAccExCalc7() != null && account.getIsAuxAccExCalc7()) {
            entry.setExCalc7(detail.getExCalc7Id());
            key.append("_exCalc7Id").append(detail.getExCalc7Id());
        }
        if (account.getIsAuxAccExCalc8() != null && account.getIsAuxAccExCalc8()) {
            entry.setExCalc8(detail.getExCalc8Id());
            key.append("_exCalc8Id").append(detail.getExCalc8Id());
        }
        if (account.getIsAuxAccExCalc9() != null && account.getIsAuxAccExCalc9()) {
            entry.setExCalc9(detail.getExCalc9Id());
            key.append("_exCalc9Id").append(detail.getExCalc9Id());
        }
        if (account.getIsAuxAccExCalc10() != null && account.getIsAuxAccExCalc10()) {
            entry.setExCalc10(detail.getExCalc10Id());
            key.append("_exCalc10Id").append(detail.getExCalc10Id());
        }
        if(account.getIsQuantityCalc() != null && account.getIsQuantityCalc()){ // 数量辅助核算时，值传给凭证，不作为分组的依据
            entry.setQuantity(detail.getCommodifyNum());
            if (amount.equals(detail.getTaxInclusiveAmount())) {
                entry.setPrice(detail.getTaxInclusivePrice());
            } else {
                entry.setPrice(detail.getPrice());
            }
        }
        // 银行账号
        if (account.getIsAuxAccBankAccount() != null && account.getIsAuxAccBankAccount()) {
            key.append("_bankAccountId").append(detail.getBankAccountId());
            entry.setBankAccountId(detail.getBankAccountId());
        }
        if (account.getIsMultiCalc() != null && account.getIsMultiCalc()) { // 多币种
            entry.setCurrencyId(org.getBaseCurrencyId());
            key.append("_currencyId").append(org.getBaseCurrencyId());
        }
        // 即征即退，影响合并
        if (account.getIsAuxAccLevyAndRetreat() != null && account.getIsAuxAccLevyAndRetreat()) {
            entry.setLevyAndRetreatId(detail.getDrawbackPolicy());
            key.append("_levyAndRetreatId").append(detail.getDrawbackPolicy());
        }

        InnerFiDocEntryDto result = new InnerFiDocEntryDto();
        result.setFiDocEntryDto(entry);
        result.setKey(key.toString());
        return result;
    }

    private void addEntry(DocEntry entry, BusinessVoucherDetail detail, BusinessVoucherSettle settle) {
        boolean isDebit = !DoubleUtil.isNullOrZero(entry.getAmountDr());
        String accountCode = entry.getAccountCode();
        List<DocEntry> entryList;
        if (isDebit) {
            if (!accountCode.startsWith("2221")) {
                entryList = debitMainList;
            } else {
                entryList = debitTaxList;
            }
        } else {
            if (!accountCode.startsWith("2221")) {
                entryList = creditMainList;
            } else {
                entryList = creditTaxList;
            }
        }
        // 结算科目颠倒借贷方向：到贷方，放到最前边；到借方，放到最后边
        if (settle != null && entry.isReversal() && !isDebit) {
            int index = 0;
            for (int size = entryList.size(); index < size; index++) {
                DocEntry item = entryList.get(index);
                if (!item.isReversal()) {
                    break;
                }
            }
            entryList.add(index, entry);
        } else {
            entryList.add(entry);
        }
    }

    public void addEntry(SettleTemplate payDocTemplate, BusinessVoucherSettle settle) {
        InnerFiDocEntryDto innerEntry = getDocEntryDto(payDocTemplate, settle);
        if (innerEntry == null) {
            return;
        }

        String key = innerEntry.getKey();
        DocEntry entry = innerEntry.getFiDocEntryDto();
        if (settle.getMerge() && entryMap.containsKey(key)) {
            handleMerge(entry, key);
        } else {
            addEntry(entry, null, settle);
            if (settle.getMerge()) {
                entryMap.put(key, entry);
            }
        }
    }

    private InnerFiDocEntryDto getDocEntryDto(SettleTemplate payDocTemplate, BusinessVoucherSettle settle) {
        Double amount = AmountGetter.getAmount(settle, payDocTemplate.getAmountSource(), settle.getAmountMap());
        if (DoubleUtil.isNullOrZero(amount)) {
            return null;
        }

        Account account = payDocTemplate.getAccount();
        DocEntry entry = new DocEntry();
        StringBuilder key = new StringBuilder(); // key 作为分录合并依据，同时单独排序时作为排序字段
        Boolean orderByFlag = voucher.getOrderByFlag();
        if (orderByFlag != null && orderByFlag) {
            key.append("_flag" + settle.getFlag()).append(orderedEntryMap.size());
            orderedEntryMap.put(key.toString(), entry);
        }
        key.append("_accountCode").append(account.getCode());
        if (account.getIsAuxAccPerson() != null && account.getIsAuxAccPerson()) { // 人员
            entry.setPersonId(settle.getPersonId());
            key.append("_personId").append(settle.getPersonId());
        }
        if (account.getIsAuxAccCustomer() != null && account.getIsAuxAccCustomer()) { // 客户
            entry.setCustomerId(settle.getCustomerId());
            key.append("_customerId").append(settle.getCustomerId());
        }
        if (account.getIsAuxAccSupplier() != null && account.getIsAuxAccSupplier()) { // 供应商
            entry.setSupplierId(settle.getSupplierId());
            key.append("_supplierId").append(settle.getSupplierId());
        }
        if (account.getIsAuxAccBankAccount() != null && account.getIsAuxAccBankAccount()) { // 银行账号
            entry.setBankAccountId(settle.getBankAccountId());
            key.append("_bankAccountId").append(settle.getBankAccountId());
        }
        if (account.getIsMultiCalc() != null && account.getIsMultiCalc()) { // 多币种
            entry.setCurrencyId(org.getBaseCurrencyId());
            key.append("_currencyId").append(org.getBaseCurrencyId());
        }

        String summary = getSettleSummary(settle, payDocTemplate);
        entry.setMergeSummary(settle.getMerge());
        entry.setSummary(summary);
        entry.setAccountId(account.getId());
        entry.setAccountCode(account.getCode());

        // 0 借 1 贷，流水账不区分币种，本币原币金额一样
        Integer direction = payDocTemplate.getBalanceDirection();
        if (direction == null) {
            direction = 0;
        }
        boolean isCr = direction != null && direction.equals(1);
        // 处理分录借贷方向、正负金额是否需要颠倒(前提：结算分录没设置不转换)
        if (payDocTemplate.getReversal() && amount < 0 && BooleanUtils.isNotFalse(settle.getReversal())) {
            isCr = !isCr;
            amount*= -1;
            entry.setReversal(true);
        }
        if (isCr) {
            entry.setAmountCr(amount);
            entry.setOrigAmountCr(amount);
        } else {
            entry.setAmountDr(amount);
            entry.setOrigAmountDr(amount);
        }

        InnerFiDocEntryDto result = new InnerFiDocEntryDto();
        result.setFiDocEntryDto(entry);
        result.setKey(key.toString());
        return result;
    }

    /**
     * 获取结算情况明细对应的分录摘要
     * @param settle
     * @param payDocTemplate
     * @return
     */
    private String getSettleSummary(BusinessVoucherSettle settle, SettleTemplate payDocTemplate) {
        String summary = settle.getRemark();
        if (!StringUtil.isEmpty(summary)) {
            return summary;
        }
        Doc doc = getDoc(); // 当非结算分录摘要全部一致时，结算分录摘要取相同值
        for (DocEntry entry : doc.getEntrys()) {
            String temp = entry.getSummary();
            if (StringUtil.isEmpty(summary)) {
                summary = temp;
            } else if (!summary.equals(temp)) {
                summary = null;
                break;
            }
        }
        if (StringUtil.isEmpty(summary)) {
            summary = payDocTemplate.getSummary();
        }
        return summary;
    }

    /**
     * 根据凭证模板和流水账收支明细获取对应的科目信息，没有获取到时返回 {@code null}
     * @param docTemplate 凭证模板信息
     * @param detail 流水账收支明细信息
     * @return 科目信息
     */
    public List<Account> getAccount(DocTemplate docTemplate, BusinessVoucherDetail detail) {
        List<Account> accounts = new ArrayList<>();
        Account account = null;
        // 采购发票生成凭证时会设置科目 id，分录 A 生成凭证时直接从发票明细上获取科目
        if (detail.getAccountId() != null && "A".equals(docTemplate.getFlag())) {
            account = accountMap.get(detail.getAccountId().toString());
            if (account != null) {
                accounts.add(account);
                return accounts;
            }
        }
        String accountKey = docTemplate.getAccountCode();
        if (StringUtil.isEmpty(accountKey)) {
            accountKey = docTemplate.getAccountId().toString();
        }
        Long accountClassification4BA = docTemplate.getAccountClassification4BA();
        String key = "";
        if (accountClassification4BA != null) { // 科目分类不为空时，先从档案上对应的科目分类获取科目
            Long archiveId = null;
            Long archiveTypeId = null;
            if (accountClassification4BA.equals(BusinessDocEngine.customer_receivableAccount)
                    || accountClassification4BA.equals(BusinessDocEngine.customer_receivableInAdvanceAccount)
                    || accountClassification4BA.equals(BusinessDocEngine.customer_otherReceivableAccount)) {
                archiveId = detail.getConsumer();
                archiveTypeId = BusinessDocEngine.archiveType_customer;
            } else if (accountClassification4BA.equals(BusinessDocEngine.supplier_payableAccount)
                    || accountClassification4BA.equals(BusinessDocEngine.supplier_payableInAdvanceAccount)
                    || accountClassification4BA.equals(BusinessDocEngine.supplier_otherPayableAccount)) {
                archiveId = detail.getVendor();
                archiveTypeId = BusinessDocEngine.archiveType_supplier;
            } else if (accountClassification4BA.equals(BusinessDocEngine.person_otherReceivableAccount)
                    || accountClassification4BA.equals(BusinessDocEngine.person_otherPayableAccount)) {
                archiveId = detail.getEmployee();
                archiveTypeId = BusinessDocEngine.archiveType_person;
            } else if (accountClassification4BA.equals(BusinessDocEngine.inventory_inventoryRelatedAccount)
                    || accountClassification4BA.equals(BusinessDocEngine.inventory_salesCostAccount)) {
                archiveId = detail.getInventory();
                archiveTypeId = BusinessDocEngine.archiveType_inventory;
            } else if (accountClassification4BA.equals(BusinessDocEngine.asset_assetAccount)
                    || accountClassification4BA.equals(BusinessDocEngine.asset_depreciationAccount)
                    || accountClassification4BA.equals(BusinessDocEngine.asset_accuDepreciationAccount)) {
                archiveId = detail.getAssetId();
                archiveTypeId = BusinessDocEngine.archiveType_asset;
            } else if (BusinessDocEngine.dept_account.contains(accountClassification4BA)) {
                archiveId = detail.getDepartment();
                archiveTypeId = BusinessDocEngine.archiveType_department;
            }
            key = archiveId + "_" + archiveTypeId + "_" + accountClassification4BA;
            if (accountMap.containsKey(key)) { // 先根据科目分类获取
                account = accountMap.get(key);
                // 处理资产分摊功能，匹配多科目的情况
                if (BusinessDocEngine.archiveType_asset.equals(archiveTypeId)) {
                    Integer index = 1;
                    String ratioKey = key + "_" + index;
                    while (accountMap.containsKey(ratioKey)) {
                        accounts.add(accountMap.get(ratioKey));
                        index++;
                        ratioKey = key + "_" + index;
                    }
                }
            }
        }
        if (account == null) {
            key = accountKey + "_" + detail.getBankAccountId();
            if (accountMap.containsKey(key)) {
                account = accountMap.get(key);
            } else {
                // 当单据明细设置了需要强制从档案的对应科目获取科目信息时，不再从凭证模板获取科目信息
                List<Long> forceUseArchiveAccountList = detail.getForceUseArchiveAccountList();
                if (account == null && (forceUseArchiveAccountList == null || !forceUseArchiveAccountList.contains(accountClassification4BA))) {
                    account = accountMap.get(accountKey);
                }
            }
        }
        if (account != null) {
            key = account.getCode() + "_" + detail.getBusinessCode();
            if (accountMap.containsKey(key)) {
                account = accountMap.get(key);
            }
        }
        if (account != null) { // 当为分摊时，取到的科目为第一行
            accounts.add(0, account);
        }
        return accounts;
    }

    /**
     * 根据结算凭证模板和流水账结算明细获取对应的科目信息，没有获取到时返回 {@code null}
     * @param settleTemplate 结算凭证模板信息
     * @param settle 流水账结算明细信息
     * @return 科目信息
     */
    public Account getAccount(SettleTemplate settleTemplate, BusinessVoucherSettle settle) {
        Account account = null;
        if (settle.getAccountId() != null) {
            account = accountMap.get(settle.getAccountId().toString());
            if (account != null) {
                return account;
            }
        }
        String accountKey = settleTemplate.getAccountCode();
        if (StringUtil.isEmpty(accountKey)) {
            accountKey = settleTemplate.getAccountId().toString();
        }
        String key = "";
        Long accountClassification4BA = settleTemplate.getAccountClassification4BA();
        if (accountClassification4BA != null) { // 科目分类不为空时，先从档案上对应的科目分类获取科目
            Long archiveId = null;
            Long archiveTypeId = null;
            if (accountClassification4BA.equals(BusinessDocEngine.customer_receivableAccount)
                    || accountClassification4BA.equals(BusinessDocEngine.customer_receivableInAdvanceAccount)
                    || accountClassification4BA.equals(BusinessDocEngine.customer_otherReceivableAccount)) {
                archiveId = settle.getCustomerId();
                archiveTypeId = BusinessDocEngine.archiveType_customer;
            } else if (accountClassification4BA.equals(BusinessDocEngine.supplier_payableAccount)
                    || accountClassification4BA.equals(BusinessDocEngine.supplier_payableInAdvanceAccount)
                    || accountClassification4BA.equals(BusinessDocEngine.supplier_otherPayableAccount)) {
                archiveId = settle.getSupplierId();
                archiveTypeId = BusinessDocEngine.archiveType_supplier;
            } else if (accountClassification4BA.equals(BusinessDocEngine.person_otherReceivableAccount)
                    || accountClassification4BA.equals(BusinessDocEngine.person_otherPayableAccount)) {
                archiveId = settle.getPersonId();
                archiveTypeId = BusinessDocEngine.archiveType_person;
            }
            key = archiveId + "_" + archiveTypeId + "_" + accountClassification4BA;
            if (accountMap.containsKey(key)) { // 先根据科目分类获取
                account = accountMap.get(key);
            }
        }
        if (account == null) {
            key = accountKey + "_" + settle.getBankAccountId();
            if (accountMap.containsKey(key)) {
                account = accountMap.get(key);
            } else {
                account = accountMap.get(accountKey);
            }
        }
        return account;
    }

    public Org getOrg() {
        return org;
    }

    public void setOrg(Org org) {
        this.org = org;
    }

    public BusinessVoucher getVoucher() {
        return voucher;
    }

    public void setVoucher(BusinessVoucher receipt) {
        this.voucher = receipt;
    }

    public Map<String, BusinessTemplate> getTemplateMap() {
        return templateMap;
    }

    public void setTemplateMap(Map<String, BusinessTemplate> templateMap) {
        this.templateMap = templateMap;
    }

    public Map<String, Account> getAccountMap() {
        return accountMap;
    }

    public void setAccountMap(Map<String, Account> accountMap) {
        this.accountMap = accountMap;
    }

    class InnerFiDocEntryDto {

        private DocEntry fiDocEntryDto;

        private String key;

        public DocEntry getFiDocEntryDto() {
            return fiDocEntryDto;
        }

        public void setFiDocEntryDto(DocEntry fiDocEntryDto) {
            this.fiDocEntryDto = fiDocEntryDto;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

    }
}
