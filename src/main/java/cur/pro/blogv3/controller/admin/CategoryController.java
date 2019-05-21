package cur.pro.blogv3.controller.admin;

import cur.pro.blogv3.constant.WebConst;
import cur.pro.blogv3.controller.BaseController;
import cur.pro.blogv3.dto.MetaDto;
import cur.pro.blogv3.dto.Types;
import cur.pro.blogv3.exception.TipException;
import cur.pro.blogv3.modal.Bo.RestResponseBo;
import cur.pro.blogv3.service.IMetaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("admin/category")
@Slf4j
public class CategoryController extends BaseController {
    @Resource
    private IMetaService metaService;

    /**
     * 标签页
     * @param request
     * @return
     */
    @GetMapping(value = "")
    public String index(HttpServletRequest request) {
        List<MetaDto> categories = metaService.getMetaList(Types.CATEGORY.getType(), null, WebConst.MAX_POSTS);
        List<MetaDto> tags = metaService.getMetaList(Types.TAG.getType(), null, WebConst.MAX_POSTS);
        request.setAttribute("categories", categories);
        request.setAttribute("tag", tags);
        return "admin/category";
    }

    /**
     * 保存标签
     * @param cname
     * @param mid
     * @return
     */
    @PostMapping(value = "save")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo saveCategory(@RequestParam String cname, @RequestParam Integer mid) {
        try {
            metaService.setMeta(Types.CATEGORY.getType(), cname, mid);

        } catch (Exception e) {
            String msg = "分类保存失败";
            if (e instanceof TipException) {
                msg = e.getMessage();
            } else {
                log.error(msg, e);
            }
            return RestResponseBo.fail(msg);
        }

        return RestResponseBo.ok();
    }


    /**
     * 删除标签
     * @param mid
     * @return
     */
    @PostMapping(value = "delete")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo delete(@RequestParam Integer mid) {
        try {
            metaService.delete(mid);

        } catch (Exception e) {
            String msg = "分类保存失败";
            if (e instanceof TipException) {
                msg = e.getMessage();
            } else {
                log.error(msg, e);
            }
            return RestResponseBo.fail(msg);
        }

        return RestResponseBo.ok();
    }
}
