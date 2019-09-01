package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface PageService {

    // 查询全部页面
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

    // 添加页面
    public CmsPageResult add(CmsPage cmsPage);

    // 根据页面id查询页面
    public CmsPage getById(String id);

    // 修改页面
    public CmsPageResult update(String id, CmsPage cmsPage);

    // 删除页面
    public ResponseResult delete(String id);

    // 静态化页面
    public String getPageHtml(String pageId);

    // 页面发布
    public ResponseResult postPage(String pageId);

    // 保存页面
    public CmsPageResult save(CmsPage cmsPage);

    // 发布页面
    public CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
