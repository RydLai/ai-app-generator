package com.yupi.yuaicodemother.core.parser;

import com.yupi.yuaicodemother.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML 单文件代码解析器
 *
 * @author yupi
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    /**
     * 兼容三种情况：正常闭合的代码块、缺少结尾围栏的代码块、只有代码没有围栏。
     */
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile(
            "```html\\s*\\r?\\n([\\s\\S]*?)(?:```|$)", Pattern.CASE_INSENSITIVE);

    private static final Pattern HTML_DOCUMENT_PATTERN = Pattern.compile(
            "(?is)<!doctype html|<html[\\s>]");

    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        // 提取 HTML 代码
        String htmlCode = extractHtmlCode(codeContent);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(ensureCompleteHtml(htmlCode.trim()));
        } else {
            // 如果没有找到代码块，将整个内容作为 HTML
            result.setHtmlCode(ensureCompleteHtml(codeContent.trim()));
        }
        return result;
    }

    /**
     * 提取 HTML 代码内容。
     *
     * 模型有时会在代码块前输出解释，甚至因为长度限制缺少结尾围栏，
     * 这里优先取围栏内的内容，取不到时再从文档声明或 html 标签开始截取。
     */
    private String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        Matcher documentMatcher = HTML_DOCUMENT_PATTERN.matcher(content);
        if (documentMatcher.find()) {
            return content.substring(documentMatcher.start());
        }
        return null;
    }

    /**
     * 兜底补全被截断的 HTML，避免浏览器把剩余内容当成未闭合标签的文本。
     */
    private String ensureCompleteHtml(String html) {
        if (html.toLowerCase().contains("</html>")) {
            return html;
        }
        StringBuilder builder = new StringBuilder(html);
        if (countTag(html, "<style") > countTag(html, "</style>")) {
            builder.append("\n</style>");
        }
        if (countTag(html, "<script") > countTag(html, "</script>")) {
            builder.append("\n</script>");
        }
        if (countTag(html, "<head") > countTag(html, "</head>")) {
            builder.append("\n</head>");
        }
        if (countTag(html, "<body") > countTag(html, "</body>")) {
            builder.append("\n</body>");
        }
        builder.append("\n</html>");
        return builder.toString();
    }

    private int countTag(String content, String tag) {
        int count = 0;
        int index = content.toLowerCase().indexOf(tag);
        while (index >= 0) {
            count++;
            index = content.toLowerCase().indexOf(tag, index + tag.length());
        }
        return count;
    }
}
