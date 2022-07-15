package com.github.outerman.be;

import java.util.Arrays;
import java.util.List;

import com.github.outerman.be.convert.DocConvertor;
import com.github.outerman.be.model.BusinessVoucher;
import com.github.outerman.be.model.ConvertResult;
import com.github.outerman.be.model.Org;
import com.github.outerman.be.template.ITemplateProvider;

/**
 * Created by shenxy on 19/7/17.
 *
 * 业务凭证引擎
 */
public final class BusinessDocEngine {

    /** 数量小数位精度，默认 6 */
    public static int QUANTITY_DECIMAL_SCALE = 6;

    /** 单价小数位精度，默认 6 */
    public static int PRICE_DECIMAL_SCALE = 6;

    /** 金额小数位精度，默认 2 */
    public static int AMOUNT_DECIMAL_SCALE = 2;

    /** 客户档案类型 id */
    public static Long archiveType_customer = 3000160001L;

    /** 供应商档案类型 id */
    public static Long archiveType_supplier = 3000160002L;

    /** 人员档案类型 id */
    public static Long archiveType_person = 3000160003L;

    /** 存货档案类型 id */
    public static Long archiveType_inventory = 3000160005L;

    /** 部门档案类型 id */
    public static Long archiveType_department = 3000160009L;

    /** 资产档案类型 id */
    public static Long archiveType_asset = 3000160013L;

    /** 客户：应收科目 */
    public static Long customer_receivableAccount = 3000150001L;

    /** 客户：预收科目 */
    public static Long customer_receivableInAdvanceAccount = 3000150002L;

    /** 客户：其他应收科目 */
    public static Long customer_otherReceivableAccount = 3000150003L;

    /** 供应商：应付科目 */
    public static Long supplier_payableAccount = 3000150004L;

    /** 供应商：预付科目 */
    public static Long supplier_payableInAdvanceAccount = 3000150005L;

    /** 供应商：其他应付科目 */
    public static Long supplier_otherPayableAccount = 3000150006L;

    /** 人员：其他应收科目 */
    public static Long person_otherReceivableAccount = 3000150007L;

    /** 人员：其他应付科目 */
    public static Long person_otherPayableAccount = 3000150008L;

    /** 存货：存货对应科目 */
    public static Long inventory_inventoryRelatedAccount = 3000150009L;

    /** 存货：销售成本科目 */
    public static Long inventory_salesCostAccount = 3000150010L;

    /** 资产：资产科目 */
    public static Long asset_assetAccount = 3000150011L;

    /** 资产：资产累计折旧/摊销科目 */
    public static Long asset_accuDepreciationAccount = 3000150012L;

    /** 资产：折旧/摊销损益科目 */
    public static Long asset_depreciationAccount = 3000150013L;

    /** 部门：计提工资 */
    public static Long dept_jtgz = 3000150014L;

    /** 部门：计提养老保险 */
    public static Long dept_jtyanglaobx = 3000150015L;

    /** 部门：计提医疗保险 */
    public static Long dept_jtyiliaobx = 3000150016L;

    /** 部门：计提失业保险 */
    public static Long dept_jtshiyebx = 3000150017L;

    /** 部门：计提生育保险 */
    public static Long dept_jtshengyubx = 3000150018L;

    /** 部门：计提工伤保险 */
    public static Long dept_jtgsbx = 3000150019L;

    /** 部门：计提补充养老保险 */
    public static Long dept_jtbcyanglaobx = 3000150020L;

    /** 部门：计提补充医疗保险 */
    public static Long dept_jtbcyiliaobx = 3000150021L;

    /** 部门：计提公积金 */
    public static Long dept_jtgjj = 3000150022L;

    /** 部门科目类型列表 */
    public static List<Long> dept_account = Arrays.asList(dept_jtgz, dept_jtyanglaobx, dept_jtyiliaobx, dept_jtshiyebx,
            dept_jtshengyubx, dept_jtgsbx, dept_jtbcyanglaobx, dept_jtbcyiliaobx, dept_jtgjj);

    private static DocConvertor convertor = DocConvertor.getInstance();

    public static ConvertResult convertVoucher(Org org, List<BusinessVoucher> vouchers, ITemplateProvider provider) {
        return convertor.convert(org, vouchers, provider);
    }

    private BusinessDocEngine() {
        // avoid instantiate
    }
}
