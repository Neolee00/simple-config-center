package com.lgblogs.server.core.mapper;

import com.lgblogs.server.core.entity.Config;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author LG
* @description 针对表【config】的数据库操作Mapper
* @createDate 2024-09-29 22:18:00
* @Entity com.lgblogs.server.core.entity.Config
*/
@Mapper
public interface ConfigMapper extends BaseMapper<Config> {

}




