package cur.pro.blogv3.service;

import cur.pro.blogv3.modal.Vo.OptionVo;

import java.util.List;
import java.util.Map;


public interface IOptionService {

    void insertOption(OptionVo optionVo);

    void insertOption(String name, String value);

    List<OptionVo> getOptions();


    /**
     * 保存一组配置
     *
     * @param options
     */
    void saveOptions(Map<String, String> options);
}
