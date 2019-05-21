package cur.pro.blogv3.controller.admin;

import com.github.pagehelper.PageInfo;
import cur.pro.blogv3.constant.WebConst;
import cur.pro.blogv3.controller.BaseController;
import cur.pro.blogv3.dto.LogActions;
import cur.pro.blogv3.dto.Types;
import cur.pro.blogv3.exception.TipException;
import cur.pro.blogv3.modal.Bo.RestResponseBo;
import cur.pro.blogv3.modal.Vo.AttachVo;
import cur.pro.blogv3.modal.Vo.UserVo;
import cur.pro.blogv3.service.IAttachService;
import cur.pro.blogv3.service.ILogServcie;
import cur.pro.blogv3.utils.Commons;
import cur.pro.blogv3.utils.TaleUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("admin/attach")
@Slf4j
public class AttachController extends BaseController {
    public static final String CLASSPATH = TaleUtils.getUplodFilePath();

    @Resource
    private IAttachService attachService;
    @Resource
    private ILogServcie logServcie;

    /**
     * 添加附件页
     * @param request
     * @param page
     * @param limit
     * @return
     */
    @GetMapping(value = "")
    public String index(HttpServletRequest request,
                        @RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "12") int limit) {
        PageInfo<AttachVo> attachVoPageInfo = attachService.getAttachs(page, limit);
        request.setAttribute("attachs", attachVoPageInfo);
        request.setAttribute(Types.ATTACH_URL.getType(), Commons.site_option(Types.ATTACH_URL.getType(), Commons.site_url()));
        request.setAttribute("max_file_size", WebConst.MAX_FILE_SIZE / 1024);

        return "admin/attach";
    }


    /**
     * 上传文件
     * @param request
     * @param multipartFiles
     * @return
     * @throws IOException
     */
    @PostMapping(value = "upload")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo upload(HttpServletRequest request, @RequestParam("file") MultipartFile[] multipartFiles)
        throws IOException {
        UserVo users = this.user(request);
        Integer uid = users.getUid();
        List<String> errorFiles = new ArrayList<>();
        try {
            for (MultipartFile multipartFile : multipartFiles) {
                String fname = multipartFile.getOriginalFilename();
                if (multipartFile.getSize() <= WebConst.MAX_FILE_SIZE) {
                    String fkey = TaleUtils.getFileKey(fname);
                    String ftype = TaleUtils.isImage(multipartFile.getInputStream()) ? Types.IMAGE.getType() : Types.FILE.getType();
                    File file = new File(CLASSPATH + fkey);
                    try {
                        FileCopyUtils.copy(multipartFile.getInputStream(), new FileOutputStream(file));
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                    attachService.save(fname, fkey, ftype, uid);
                } else {
                    errorFiles.add(fname);
                }
            }
        } catch (Exception e) {
            return RestResponseBo.fail();
        }

        return RestResponseBo.ok(errorFiles);
    }

    @PostMapping(value = "delete")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo delete(@RequestParam Integer id, HttpServletRequest request) {
        try {
            AttachVo attach = attachService.selectById(id);
            if (null == attach) {
                return RestResponseBo.fail("不存在该附件");
            }

            attachService.deleteById(id);
            new File(CLASSPATH + attach.getFkey()).delete();
            logServcie.insertLog(LogActions.DEL_ARTICLE.getAction(), attach.getFkey(), request.getRemoteAddr(), this.getUid(request));

        } catch (Exception e) {
            String msg = "附件删除失败";
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
