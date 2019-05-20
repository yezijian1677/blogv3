package cur.pro.blogv3.service;

import com.github.pagehelper.PageInfo;
import cur.pro.blogv3.modal.Vo.AttachVo;

/**
 * 附件
 */
public interface IAttachService {

    /**
     * 分页查询
     *
     * @param page
     * @param limit
     * @return
     */
    PageInfo<AttachVo> getAttachs(Integer page, Integer limit);

    /**
     * 保存附件
     *
     * @param fname
     * @param fkey
     * @param ftype
     * @param author
     */
    void save(String fname, String fkey, String ftype, Integer author);

    /**
     * 根据id查询附件
     *
     * @param id
     * @return
     */
    AttachVo selectById(Integer id);

    /**
     * 根据id删除附件
     *
     * @param id
     */
    void deleteById(Integer id);
}
