package cur.pro.blogv3.service.impl;

import com.github.pagehelper.PageHelper;
import cur.pro.blogv3.constant.WebConst;
import cur.pro.blogv3.dao.LogVoMapper;
import cur.pro.blogv3.modal.Vo.LogVo;
import cur.pro.blogv3.modal.Vo.LogVoExample;
import cur.pro.blogv3.service.ILogServcie;
import cur.pro.blogv3.utils.DateKit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
@Service
@Slf4j
public class LogServiceImpl implements ILogServcie {

    @Resource
    private LogVoMapper logVoMapper;

    @Override
    public void insertLog(LogVo logVo) {
        logVoMapper.insert(logVo);
    }

    @Override
    public void insertLog(String action, String data, String ip, Integer authorId) {
        LogVo logs = new LogVo();
        logs.setAction(action);
        logs.setData(data);
        logs.setIp(ip);
        logs.setAuthorId(authorId);
        logs.setCreated(DateKit.getCurrentUnixTime());
        logVoMapper.insert(logs);
    }

    @Override
    public List<LogVo> getLogs(int page, int limit) {
        log.debug("Enter get Logs method: page={}, limit={}", page, limit);
        if (page <= 0) {
            page = 1;
        }
        if (limit < 1 || limit > WebConst.MAX_POSTS) {
            limit = 10;
        }

        LogVoExample logVoExample = new LogVoExample();
        logVoExample.setOrderByClause("id desc");
        PageHelper.startPage((page - 1) * limit, limit);
        List<LogVo> logVos = logVoMapper.selectByExample(logVoExample);
        log.debug("Exit getLogs method");

        return logVos;
    }
}
