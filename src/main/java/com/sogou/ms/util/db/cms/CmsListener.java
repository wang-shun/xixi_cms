package com.sogou.ms.util.db.cms;

import java.util.List;
import java.util.Map;

/**
 * User:Jarod
 */
public interface CmsListener {
    public void change(List<Map<String, String>> cmsData);
}
