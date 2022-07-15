
# 业务引擎核心项目

## 1、项目目标:
1. 各种异构业务数据, 生成财务凭证
2. 有良好的扩展性
3. 对扩展和自定义的业务模板, 可验证正确性


## 2、当前完成功能:
1. 项目独立于现有业务系统
2. 模板基本验证逻辑
3. 使用现有模板生成凭证

## 3、待完成功能:
0. 与现有项目的非源码的继承
1. 完整的模板静态验证
2. 完整的凭证生成的验证逻辑
3. 项目内的凭证验证过程: 生成业务dto -> 审核生成凭证 -> 凭证正确性验证
4. 上层业务系统, 不同DTO的无缝支持 
5. 模板的扩展设计: 影响因素\取数公式\行业
6. 前端维护界面, 支持系统级的运营支持, 可增删改\合并等处理
7. 支持用户自定义扩展个性化模板
8. 模板数据在工程内存储
9. 对于dto是否符合业务模板的通用校验(业务对象从Excel导入等场景)

10. 金额表达式调整，符合 EL 语法规则（一个参考规范，JEXL 实现）



## 4、附:
### 模板静态校验规则(持续扩展):
1. 元数据模板规则：`AcmUITemplate`
    1. 票据类型至少在一种纳税人身份下显示
    2. 银行账号（结算方式）、税率（征收率）显示时，特殊输入规则必须有对应的数据
    3. 存货、资产类别显示时，对应存货属性、资产类别（存货属性细分）必须要有对应数据
    4. 票据类型没有选择任何行业（只能在 excel 导入时处理）
2. 凭证模板规则：`AcmDocAccountTemplate`
    1. 影响因素取值有数据时，影响因素必须要有数据，单独排序除外；影响因素有值时，对应的取值必须有数据；// excel 模板导入、自定义新增修改时进行校验
    2. 同一分组纳税人、计税方式，纳税人、认证影响因素需要两条记录
    3. 金额来源表达式正确性校验 // TODO 金额表达式正确性校验在金额表达式调整（待完成功能10）之后再验证
    4. 同一分组相同影响因素取值只能有一条记录 // 同一分组影响因素相同，金额取值，科目，借贷方向等不同可以取到多条记录，也是支持的，暂不校验
    5. 凭证模板科目在当前会计准则、行业、纳税人身份需要可用 // TODO，现有科目获取接口需要组织 id，不满足需求，需要再调整
3. 交叉规则
    1. 元数据模板银行账号（结算方式）显示（现在显示即必填）时，凭证模板对方科目来源必须存在结算方式数据 `TemplateSettleValidator`
    2. 凭证模板影响因素没有默认记录，则元数据模板对应的字段必填；存货属性影响因素，存货必填且没有默认时，和业务类型存货属性对照表校验 `TemplateInfluenceValidator`
    3. 元数据模板有数据，凭证模板（两种会计准则）必须有对应行业的数据；凭证模板有数据，元数据模板必须有对应行业的数据 `TemplateIndustryValidator`
    4. 凭证模板金额来源表达式使用的字段需要元数据模板对应的字段显示 `TemplateFundsourceValidator`
    5. 凭证模板影响因素需要元数据模板对应的字段显示 `TemplateInfluenceValidator`
4. 业务类型税务属性规则：收入类型对应属性表
    1. 收入类型的业务，业务类型税务属性对照表需要有对应数据，用于税务取数 // TODO 需要税务属性的业务是根据具体需求确定无法验证
5. 业务类型存货属性规则：业务类型存货关系表
    1. 元数据模板存货、资产显示时，业务类型存货属性表需要有对应数据，用于填写流水账时控制可选存货范围 `AcmUITemplate.validateAsset`
    2. 凭证模板影响因素有存货属性的，则影响因素取值需要和业务类型存货属性对照表一致或者有默认记录 // TODO 对照表不区分行业，凭证模板区分，现有实现规则无法验证

6. 凭证模板存在结算方式平，业务类型结算需要勾选 // TODO

## 生成凭证校验规则(持续扩展):
1. 构建dto的模拟数据  
2. 遍历各个影响因素  
3. 根据模板验证凭证科目和金额是否正确`DocEntryValidator`  