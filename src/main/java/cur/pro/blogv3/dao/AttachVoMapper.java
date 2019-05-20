package cur.pro.blogv3.dao;

import cur.pro.blogv3.modal.Vo.AttachVo;
import cur.pro.blogv3.modal.Vo.AttachVoExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface AttachVoMapper {

    long countByExample(AttachVoExample example);

    int deleteByExample(AttachVoExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AttachVo attachVo);

    int insertSelective(AttachVo attachVo);

    List<AttachVo> selectByExample(AttachVoExample example);

    AttachVo selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AttachVo recode, @Param("example") AttachVoExample example);

    int updateByExample(@Param("recode") AttachVo record, @Param("example") AttachVoExample example);

    int updateByPrimaryKeySelective(AttachVo attachVo);

    int updateByPrimaryKey(AttachVo record);
}
