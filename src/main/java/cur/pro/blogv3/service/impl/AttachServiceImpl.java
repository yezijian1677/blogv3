package cur.pro.blogv3.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import cur.pro.blogv3.dao.AttachVoMapper;
import cur.pro.blogv3.modal.Vo.AttachVo;
import cur.pro.blogv3.modal.Vo.AttachVoExample;
import cur.pro.blogv3.service.IAttachService;
import cur.pro.blogv3.utils.DateKit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class AttachServiceImpl implements IAttachService {

    @Resource
    private AttachVoMapper attachVoMapper;


    @Override
    public PageInfo<AttachVo> getAttachs(Integer page, Integer limit) {
        PageHelper.startPage(page, limit);
        AttachVoExample example = new AttachVoExample();
        example.setOrderByClause("id desc");
        List<AttachVo> attachVos = attachVoMapper.selectByExample(example);
        return new PageInfo<>(attachVos);
    }

    @Override
    public void save(String fname, String fkey, String ftype, Integer author) {
        AttachVo attachVo = new AttachVo();
        attachVo.setFkey(fname);
        attachVo.setAuthorId(author);
        attachVo.setFkey(fkey);
        attachVo.setFtype(ftype);
        attachVo.setCreated(DateKit.getCurrentUnixTime());
        attachVoMapper.insertSelective(attachVo);
    }

    @Override
    public AttachVo selectById(Integer id) {
        if (null != id) {
            return attachVoMapper.selectByPrimaryKey(id);
        }
        return null;
    }

    @Override
    public void deleteById(Integer id) {
        if (null != id) {
            attachVoMapper.deleteByPrimaryKey(id);
        }
    }
}
