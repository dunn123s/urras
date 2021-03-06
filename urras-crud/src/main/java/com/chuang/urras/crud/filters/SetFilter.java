package com.chuang.urras.crud.filters;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chuang.urras.support.Result;
import com.chuang.urras.support.exception.SystemWarnException;
import com.chuang.urras.toolskit.basic.util.Convert;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ath on 2018/4/29.
 */
@Slf4j
@Data
public class SetFilter implements RowQuery.Filter {

    private String field;
    private String option;
    private String[] values;
    @Override
    public String getField() {
        return field;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SET;
    }


    @Override
    public <T> void handle(QueryWrapper<T> criteria, Class<T> objClazz) {
        if(values.length == 0) {
            throw new SystemWarnException(Result.FAIL_CODE, field + "至少要选一项进行查询。否则查询无意义。");
        }

        List<Object> list = new ArrayList<>();
        try {
            Class<?> fieldType = objClazz.getDeclaredField(field).getType();
            if(fieldType == String.class) {
                list =  Arrays.asList(values);
            } else if(Enum.class.isAssignableFrom(fieldType)) {
                for (String v: values) {
                    Enum e =  Enum.valueOf((Class<Enum>)fieldType, v);
                    list.add(e);
                }
            } else {
                for(String v: values) {
                    list.add(Convert.parseBasic(fieldType, v));
                }
            }

        } catch (NoSuchFieldException e) {
            log.error("set filter error, 这里字符串方式", e);
            list =  Arrays.asList(values);
        }

        String _field = StringUtils.camelToUnderline(field);
        if("in".equalsIgnoreCase(option)) {
            criteria.in(_field, list);
        } else if("notIn".equalsIgnoreCase(option)) {
            criteria.notIn(_field, list);
        }
    }
}
