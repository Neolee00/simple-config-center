package com.lgblogs.server.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lgblogs.server.core.entity.Config;
import com.lgblogs.server.core.mapper.ConfigMapper;
import com.lgblogs.server.core.service.ConfigService;
import org.springframework.stereotype.Service;

/**
* @author LG
* @description 针对表【config】的数据库操作Service实现
* @createDate 2024-09-29 22:18:00
*/
@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config>
    implements ConfigService {

}




