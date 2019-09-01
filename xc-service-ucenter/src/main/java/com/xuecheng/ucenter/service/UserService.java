package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    XcUserRepository xcUserRepository;

    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;

    @Autowired
    XcMenuMapper xcMenuMapper;

    public XcUserExt getUserExt(String username){
        XcUser xcUser = this.findXcUserByUsername(username);
        String userId = xcUser.getId();
        if(xcUser == null){
            return null;
        }
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findByUserId(userId);
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userId);
        if(xcCompanyUser == null){
            return null;
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        xcUserExt.setCompanyId(xcCompanyUser.getId());
        // 用户权限
        xcUserExt.setPermissions(xcMenus);

        return xcUserExt;

    }

    private XcUser findXcUserByUsername(String username){
        return xcUserRepository.findXcUsersByUsername(username);
    }


}
