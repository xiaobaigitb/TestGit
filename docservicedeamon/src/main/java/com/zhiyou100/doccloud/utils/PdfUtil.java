package com.zhiyou100.doccloud.utils;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class PdfUtil {


    /**
     * 通过PDFbox获取文章总页数
     *
     * @param filePath:文件路径
     * @return
     * @throws IOException
     */
    public static int getNumberOfPages(String filePath) throws IOException {
        System.out.println(filePath);
        PDDocument pdDocument = PDDocument.load(new File(filePath));

        int pages = pdDocument.getNumberOfPages();
        pdDocument.close();
        return pages;
    }

    /**
     * 通过PDFbox获取文章内容
     *
     * @param filePath
     * @return
     */
    public static String getContent(String filePath) throws IOException {
        PDFParser pdfParser = new PDFParser(new org.apache.pdfbox.io.RandomAccessFile(new File(filePath), "rw"));
        pdfParser.parse();
        PDDocument pdDocument = pdfParser.getPDDocument();
        String text = new PDFTextStripper().getText(pdDocument);
        pdDocument.close();

        return text;
    }

    /**
     * 通过PDFbox生成文件的缩略图
     *
     * @param filePath：文件路径
     * @param outPath：输出图片路径
     * @throws IOException
     */
    public static void getThumbnails(String filePath, String outPath) throws IOException {
        // 利用PdfBox生成图像
        PDDocument pdDocument = PDDocument.load(new File(filePath));
        PDFRenderer renderer = new PDFRenderer(pdDocument);

        // 构造图片
        BufferedImage img_temp = renderer.renderImageWithDPI(0, 30, ImageType.RGB);
        // 设置图片格式
        Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix("png");
        // 将文件写出
        ImageWriter writer = (ImageWriter) it.next();
        ImageOutputStream imageout = ImageIO.createImageOutputStream(new FileOutputStream(outPath));
        writer.setOutput(imageout);
        writer.write(new IIOImage(img_temp, null, null));
        img_temp.flush();
        imageout.flush();
        imageout.close();
        //Warning: You did not close a PDF Document
        pdDocument.close();
    }

    public static void main(String[] args) throws IOException {
        //int numberOfPages = getNumberOfPages("D:\\tmp\\docjobdaemon\\cdbacbf5-e6cb-4d48-9f17-32c9d86e07c7\\hadoopInstall.pdf");
        //System.out.println(numberOfPages);

        //getThumbnails("D:\\tmp\\docjobdaemon\\cdbacbf5-e6cb-4d48-9f17-32c9d86e07c7\\hadoopInstall.pdf","D:\\tmp\\docjobdaemon\\cdbacbf5-e6cb-4d48-9f17-32c9d86e07c7\\hadoopInstall.png");

    }
}

