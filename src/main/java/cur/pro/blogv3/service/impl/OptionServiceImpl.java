package cur.pro.blogv3.service.impl;

import cur.pro.blogv3.dao.OptionVoMapper;
import cur.pro.blogv3.modal.Vo.OptionVo;
import cur.pro.blogv3.modal.Vo.OptionVoExample;
import cur.pro.blogv3.service.IOptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OptionServiceImpl implements IOptionService {

    @Resource
    private OptionVoMapper optionVoMapper;

    @Override
    public void insertOption(OptionVo optionVo) {
        log.debug("Enter insertOption method:optionVo={}", optionVo);
        optionVoMapper.insertSelective(optionVo);
        log.debug("Exit insertOption method.");

    }

    @Override
    public void insertOption(String name, String value) {
        log.debug("Enter insertOption method:name={}, value={}", name, value);
        OptionVo optionVo = new OptionVo();
        optionVo.setName(name);
        optionVo.setValue(value);
        if (optionVoMapper.selectByExample(new OptionVoExample()).size() == 0) {
            optionVoMapper.insertSelective(optionVo);
        } else {
            optionVoMapper.updateByPrimaryKeySelective(optionVo);
        }

        log.debug("Exit insertOption method.");
    }

    @Override
    public List<OptionVo> getOptions() {
        return optionVoMapper.selectByExample(new OptionVoExample());
    }

    @Override
    public void saveOptions(Map<String, String> options) {
        if (null != options && !options.isEmpty()) {
            options.forEach(this::insertOption);
        }
    }
}
