package cur.pro.blogv3.service;

import cur.pro.blogv3.dto.MetaDto;
import cur.pro.blogv3.modal.Vo.MetaVo;

import java.util.List;

public interface IMetaService {

    /**
     * 根据类型和名字查询
     *
     * @param type
     * @param name
     * @return
     */
    MetaDto getMeta(String type, String name);

    /**
     * 根据id查询项目个数
     *
     * @param mid
     * @return
     */
    Integer countMeta(Integer mid);


    /**
     * 根据类型查询项目列表
     *
     * @param types
     * @return
     */
    List<MetaVo> getMetas(String types);

    /**
     * 保存多个项目
     *
     * @param cid
     * @param names
     * @param type
     */
    void setMetas(Integer cid, String names, String type);

    /**
     * 保存项目
     *
     * @param type
     * @param name
     * @param mid
     */
    void setMeta(String type, String name, Integer mid);

    /**
     * 根据类型获取项目列表
     * 带项目下的文章数
     *
     * @param type
     * @param orderBy
     * @param limit
     * @return
     */
    List<MetaDto> getMetaList(String type, String orderBy, int limit);

    /**
     * 删除
     *
     * @param mid
     */
    void delete(int mid);

    /**
     * 保存
     *
     * @param metas
     */
    void saveMeta(MetaVo metas);

    /**
     * 修改
     *
     * @param metaVo
     */
    void update(MetaVo metaVo);




}
