package com.github.outerman.be.model;

import java.io.Serializable;

public class DocEntry implements Serializable {

    private static final long serialVersionUID = 4609809951218436168L;

    /** 摘要[set_Summary]  */
    private String summary;

    /** 科目ID[set_account]  */
    private Long accountId;

    private String accountCode;

    /** 借方原币  */
    private Double origAmountDr;

    /** 贷方原币  */
    private Double origAmountCr;

    /** 借方本币  */
    private Double amountDr;

    /** 贷方本币  */
    private Double amountCr;

    /** 数量  */
    private Double quantity;

    /** 单价  */
    private Double price;

    /** 计量单位id[set_unit]  */
    private Long unitId;

    /** 币种ID[set_currency]  */
    private Long currencyId;

    /** 汇率  */
    private Double exchangeRate;

    /** 部门id[set_department]  */
    private Long departmentId;

    /** 人员id[set_person]  */
    private Long personId;

    /** 客户ID  */
    private Long customerId;

    /** 供应商ID  */
    private Long supplierId;

    /** 存货id[set_inventory]  */
    private Long inventoryId;

    /** 项目ID[set_project]  */
    private Long projectId;

    /** 账号ID[set_bank_account]  */
    private Long bankAccountId;

    /** 自定义档案  */
    private Long exCalc1;

    /** 自定义档案  */
    private Long exCalc2;

    /** 自定义档案  */
    private Long exCalc3;

    /** 自定义档案  */
    private Long exCalc4;

    /** 自定义档案  */
    private Long exCalc5;

    /** 自定义档案  */
    private Long exCalc6;

    /** 自定义档案  */
    private Long exCalc7;

    /** 自定义档案  */
    private Long exCalc8;

    /** 自定义档案  */
    private Long exCalc9;

    /** 自定义档案  */
    private Long exCalc10;

    /** 即征即退项目ID  */
    private Long levyAndRetreatId;

    /** 来源单据业务类型  */
    private Long sourceBusinessTypeId;

    /** 来源单据 模板中标识取值  */
    private String sourceFlag;

    /** 方向：0：借，1:贷  */
    private byte direction;

    /** 是否颠倒了借贷方向 */
    private boolean reversal = false;

    /** 摘要[set_Summary]  */
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    /** 汇率  */
    public Double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(Double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    /** 借方原币  */
    public Double getOrigAmountDr() {
        return origAmountDr;
    }

    public void setOrigAmountDr(Double origAmountDr) {
        this.origAmountDr = origAmountDr;
    }

    /** 贷方原币  */
    public Double getOrigAmountCr() {
        return origAmountCr;
    }

    public void setOrigAmountCr(Double origAmountCr) {
        this.origAmountCr = origAmountCr;
    }

    /** 借方本币  */
    public Double getAmountDr() {
        return amountDr;
    }

    public void setAmountDr(Double amountDr) {
        this.amountDr = amountDr;
    }

    /** 贷方本币  */
    public Double getAmountCr() {
        return amountCr;
    }

    public void setAmountCr(Double amountCr) {
        this.amountCr = amountCr;
    }

    /** 贷方  */
    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    /** 单价  */
    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    /** 计量单位id[set_unit]  */
    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    /** 币种ID[set_currency]  */
    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
    }

    /** 科目ID[set_account]  */
    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    /** 部门id[set_department]  */
    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    /** 人员id[set_person]  */
    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    /** 客户ID  */
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    /** 供应商ID  */
    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    /** 存货id[set_inventory]  */
    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    /** 项目ID[set_project]  */
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /** 账号ID[set_bank_account]  */
    public Long getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(Long bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    /** 科目Code[set_account]  */
    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public Long getLevyAndRetreatId() {
        return levyAndRetreatId;
    }

    public void setLevyAndRetreatId(Long levyAndRetreatId) {
        this.levyAndRetreatId = levyAndRetreatId;
    }

    public byte getDirection() {
        return direction;
    }

    public void setDirection(byte direction) {
        this.direction = direction;
    }

    /**
     * 获取是否颠倒了借贷方向
     * @return 是否颠倒了借贷方向
     */
    public boolean isReversal() {
        return reversal;
    }

    /**
     * 设置是否颠倒了借贷方向
     * @param reversal 是否颠倒了借贷方向
     */
    public void setReversal(boolean reversal) {
        this.reversal = reversal;
    }

    public Long getSourceBusinessTypeId() {
        return sourceBusinessTypeId;
    }

    public void setSourceBusinessTypeId(Long sourceBusinessTypeId) {
        this.sourceBusinessTypeId = sourceBusinessTypeId;
    }

    public String getSourceFlag() {
        return sourceFlag;
    }

    public void setSourceFlag(String sourceFlag) {
        this.sourceFlag = sourceFlag;
    }

    public Long getExCalc1() {
        return exCalc1;
    }

    public void setExCalc1(Long exCalc1) {
        this.exCalc1 = exCalc1;
    }

    public Long getExCalc2() {
        return exCalc2;
    }

    public void setExCalc2(Long exCalc2) {
        this.exCalc2 = exCalc2;
    }

    public Long getExCalc3() {
        return exCalc3;
    }

    public void setExCalc3(Long exCalc3) {
        this.exCalc3 = exCalc3;
    }

    public Long getExCalc4() {
        return exCalc4;
    }

    public void setExCalc4(Long exCalc4) {
        this.exCalc4 = exCalc4;
    }

    public Long getExCalc5() {
        return exCalc5;
    }

    public void setExCalc5(Long exCalc5) {
        this.exCalc5 = exCalc5;
    }

    public Long getExCalc6() {
        return exCalc6;
    }

    public void setExCalc6(Long exCalc6) {
        this.exCalc6 = exCalc6;
    }

    public Long getExCalc7() {
        return exCalc7;
    }

    public void setExCalc7(Long exCalc7) {
        this.exCalc7 = exCalc7;
    }

    public Long getExCalc8() {
        return exCalc8;
    }

    public void setExCalc8(Long exCalc8) {
        this.exCalc8 = exCalc8;
    }

    public Long getExCalc9() {
        return exCalc9;
    }

    public void setExCalc9(Long exCalc9) {
        this.exCalc9 = exCalc9;
    }

    public Long getExCalc10() {
        return exCalc10;
    }

    public void setExCalc10(Long exCalc10) {
        this.exCalc10 = exCalc10;
    }
}
