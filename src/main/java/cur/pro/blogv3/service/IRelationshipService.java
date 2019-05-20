package cur.pro.blogv3.service;

import cur.pro.blogv3.modal.Vo.RelationshipVoKey;

import java.util.List;

public interface IRelationshipService {
    /**
     * 按主键删除
     *
     * @param cid
     * @param mid
     */
    void deleteById(Integer cid, Integer mid);

    /**
     * 按主键统计条数
     *
     * @param cid
     * @param mid
     * @return 条数
     */
    Long countById(Integer cid, Integer mid);


    /**
     * 保存对象
     *
     * @param relationshipVoKey
     */
    void insertVo(RelationshipVoKey relationshipVoKey);

    /**
     * 根据id搜索
     *
     * @param cid
     * @param mid
     * @return
     */
    List<RelationshipVoKey> getRelationshipById(Integer cid, Integer mid);
}
