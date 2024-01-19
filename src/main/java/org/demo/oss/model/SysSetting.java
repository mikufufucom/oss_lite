package org.demo.oss.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 系统设置表
 */
@Data
@AllArgsConstructor
@TableName(value = "sys_setting")
public class SysSetting implements Serializable {
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 配置编号
     */
    @TableField(value = "code")
    private String code;

    /**
     * 配置名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 配置描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 配置的值
     */
    @TableField(value = "`value`")
    private String value;

    /**
     * 是否启用
     */
    @TableField(value = "`status`")
    private Integer status;

    private static final long serialVersionUID = 1L;
}