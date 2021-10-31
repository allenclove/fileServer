package cn.alenc.controller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

/**
 * @ClassName UploadController
 * @Description
 * @Author ALLENC
 * @Date 2021/10/31 15:56
 **/
@Controller
public class UploadController {

    private final static String utf8 = "utf-8";

    @RequestMapping("/upload")
    @ResponseBody
    @CrossOrigin
    public void upload(HttpServletRequest request, HttpServletResponse response) throws Exception {

        //分片
        response.setCharacterEncoding(utf8);
        Integer schunk = null;
        Integer schunks = null;
        String name = null;
        String uploadPath = "E:\\java_project\\fileItem";
        BufferedOutputStream os = null;
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            //设置缓冲区
            factory.setSizeThreshold(1024);
            factory.setRepository(new File(uploadPath));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(5L * 1024L * 1024L * 1024L);
            upload.setSizeMax(10L * 1024L * 1024L * 1024L);
            List<FileItem> items = upload.parseRequest(request);

            for (FileItem item : items) {
                //判断是不是文件对象
                if(item.isFormField()){
                    if("chunk".equals(item.getFieldName())){
                        schunk = Integer.parseInt(item.getString(utf8));
                    }

                    if("chunks".equals(item.getFieldName())){
                        schunks = Integer.parseInt(item.getString(utf8));
                    }

                    if("name".equals(item.getFieldName())){
                        name = item.getString(utf8);
                    }
                }
            }

            for (FileItem item : items) {
                if(!item.isFormField()){
                    String temFileName = name;
                    if(name != null){
                        if(schunk != null){
                            temFileName = schunk + "_" + name;
                        }
                        File temFile = new File(uploadPath,temFileName);
                        //断点续传
                        if (!temFile.exists()){
                            item.write(temFile);
                        }
                    }
                }
            }

            if(schunk != null && schunk.intValue() == schunks.intValue() - 1){
                File tempFile = new File(uploadPath,name);
                os= new BufferedOutputStream(new FileOutputStream(tempFile));

                for (Integer i = 0; i < schunks; i++) {
                    File file = new File(uploadPath, i + "_" + name);
                    while (!file.exists()) {
                        Thread.sleep(100);
                    }
                    byte[] bytes = FileUtils.readFileToByteArray(file);
                    os.write(bytes);
                    os.flush();
                    file.delete();
                }
                os.flush();
            }
            response.getWriter().write("文件上传成功：" + name);

        }finally {
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
