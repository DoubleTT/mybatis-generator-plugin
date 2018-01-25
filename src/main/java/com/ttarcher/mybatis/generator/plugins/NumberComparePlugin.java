package com.ttarcher.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.ModelColumnPlugin;
import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.PluginTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * 比较增强插件
 *
 * @author Todd
 * @date 2018/1/25
 */
public class NumberComparePlugin extends BasePlugin {
    /**
     * Number Enum Name
     */
    private static final String ENUM_NAME = "NumberColumn";

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addInnerEnum(generateNumberColumnEnum(topLevelClass, introspectedTable));
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addInnerEnum(generateNumberColumnEnum(topLevelClass, introspectedTable));
        return super.modelRecordWithBLOBsClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addInnerEnum(generateNumberColumnEnum(topLevelClass, introspectedTable));
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        List<InnerClass> innerClasses = topLevelClass.getInnerClasses();
        for (InnerClass innerClass : innerClasses) {
            if ("Criteria".equals(innerClass.getType().getShortName())) {
                addEnhancedCompare(innerClass, introspectedTable);
            }
        }
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    private InnerEnum generateNumberColumnEnum(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 生成内部枚举
        InnerEnum innerEnum = new InnerEnum(new FullyQualifiedJavaType(ENUM_NAME));
        innerEnum.setVisibility(JavaVisibility.PUBLIC);
        innerEnum.setStatic(true);
        commentGenerator.addEnumComment(innerEnum, introspectedTable);
        logger.debug("ttarcher(数据Model属性对应Column获取插件):" + topLevelClass.getType().getShortName() + "增加内部Builder类。");

        // 生成属性和构造函数
        Field numberColumnField = new Field("numberColumn", FullyQualifiedJavaType.getStringInstance());
        numberColumnField.setVisibility(JavaVisibility.PRIVATE);
        numberColumnField.setFinal(true);
        commentGenerator.addFieldComment(numberColumnField, introspectedTable);
        innerEnum.addField(numberColumnField);

        Method mValue = new Method("value");
        mValue.setVisibility(JavaVisibility.PUBLIC);
        mValue.setReturnType(FullyQualifiedJavaType.getStringInstance());
        mValue.addBodyLine("return this.numberColumn;");
        commentGenerator.addGeneralMethodComment(mValue, introspectedTable);
        innerEnum.addMethod(mValue);

        Method mGetValue = new Method("getValue");
        mGetValue.setVisibility(JavaVisibility.PUBLIC);
        mGetValue.setReturnType(FullyQualifiedJavaType.getStringInstance());
        mGetValue.addBodyLine("return this.numberColumn;");
        commentGenerator.addGeneralMethodComment(mGetValue, introspectedTable);
        innerEnum.addMethod(mGetValue);

        Method constructor = new Method(ENUM_NAME);
        constructor.setConstructor(true);
        constructor.addBodyLine("this.numberColumn = numberColumn;");
        constructor.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "numberColumn"));
        commentGenerator.addGeneralMethodComment(constructor, introspectedTable);
        innerEnum.addMethod(constructor);
        logger.debug("ttarcher(数据Model属性对应Column获取插件):" + topLevelClass.getType().getShortName() + ".Column增加构造方法和numberColumn属性。");

        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            if (isNumber(introspectedColumn.getFullyQualifiedJavaType())) {
                Field field = JavaBeansUtil.getJavaBeansField(introspectedColumn, context, introspectedTable);
                String sb = field.getName() +
                        "(\"" +
                        introspectedColumn.getActualColumnName() +
                        "\")";
                innerEnum.addEnumConstant(sb);
                logger.debug("ttarcher(数据Model属性对应NumberColumn获取插件):" + topLevelClass.getType().getShortName() + ".Column增加" + field.getName() + "枚举。");
            }
        }
        return innerEnum;
    }

    private void addEnhancedCompare(InnerClass innerClass, IntrospectedTable introspectedTable) {
        introspectedTable.getAllColumns().forEach(column -> {
            if (isNumber(column.getFullyQualifiedJavaType())) {
                innerClass.addMethod(addEqualToMethod(introspectedTable, column));
                innerClass.addMethod(addNotEqualToMethod(introspectedTable, column));
                innerClass.addMethod(addGreaterThan(introspectedTable, column));
                innerClass.addMethod(addGreaterThanOrEqualTo(introspectedTable, column));
                innerClass.addMethod(addLessThan(introspectedTable, column));
                innerClass.addMethod(addLessThanOrEqualTo(introspectedTable, column));
            }
        });
    }


    private boolean isNumber(FullyQualifiedJavaType type) {
        boolean isInteger = PrimitiveTypeWrapper.getIntegerInstance().equals(type);
        boolean isLong = PrimitiveTypeWrapper.getLongInstance().equals(type);
        boolean isByte = PrimitiveTypeWrapper.getByteInstance().equals(type);
        boolean isDouble = PrimitiveTypeWrapper.getDoubleInstance().equals(type);
        boolean isCharacter = PrimitiveTypeWrapper.getCharacterInstance().equals(type);
        boolean isFloat = PrimitiveTypeWrapper.getFloatInstance().equals(type);

        return isInteger || isLong || isByte || isDouble || isCharacter || isFloat;
    }

    private Method addEqualToMethod(IntrospectedTable introspectedTable, IntrospectedColumn column) {
        return createCompareCriterionMethod(introspectedTable, column, "EqualTo", "=");
    }

    private Method addNotEqualToMethod(IntrospectedTable introspectedTable, IntrospectedColumn column) {
        return createCompareCriterionMethod(introspectedTable, column, "NotEqualTo", "<>");
    }

    private Method addGreaterThan(IntrospectedTable introspectedTable, IntrospectedColumn column) {
        return createCompareCriterionMethod(introspectedTable, column, "GreaterThan", ">");
    }

    private Method addGreaterThanOrEqualTo(IntrospectedTable introspectedTable, IntrospectedColumn column) {
        return createCompareCriterionMethod(introspectedTable, column, "GreaterThanOrEqualTo", ">=");
    }

    private Method addLessThan(IntrospectedTable introspectedTable, IntrospectedColumn column) {
        return createCompareCriterionMethod(introspectedTable, column, "LessThan", "<");
    }

    private Method addLessThanOrEqualTo(IntrospectedTable introspectedTable, IntrospectedColumn column) {
        return createCompareCriterionMethod(introspectedTable, column, "LessThanOrEqualTo", "<=");
    }


    private Method createCompareCriterionMethod(IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn,
                                                String nameFragment, String operator) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);


        String modelName = introspectedTable.getFullyQualifiedTable().getDomainObjectName() + "." + ENUM_NAME;
        method.addParameter(new Parameter(new FullyQualifiedJavaType(modelName), "column"));

        StringBuilder sb = new StringBuilder();
        sb.append(introspectedColumn.getJavaProperty());
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.insert(0, "and");
        sb.append(nameFragment);
        method.setName(sb.toString());
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
        sb.setLength(0);

        if (stringHasValue(introspectedColumn.getTypeHandler())) {
            sb.append("add");
            sb.append(introspectedColumn.getJavaProperty());
            sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
            sb.append("Criterion(\"");
        } else {
            sb.append("addCriterion(\"");
        }

        sb.append(MyBatis3FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
        sb.append(' ');
        sb.append(operator);
        sb.append(" \" + ");
        sb.append("column.value());");
        method.addBodyLine(sb.toString());
        method.addBodyLine("return (Criteria) this;");

        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        return method;
    }
}
