package com.design.museum.aop;

import cn.dev33.satoken.stp.StpUtil;
import com.design.museum.entity.UserNotice;
import com.design.museum.mapper.UserNoticeMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 消息通知切面
 */
@Aspect
@Component
@Slf4j
public class NoticeAspect {

    @Resource
    private UserNoticeMapper userNoticeMapper;

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 拦截带 @Notice 注解的方法，在方法返回后发送站内消息
     */
    @AfterReturning(pointcut = "@annotation(notice)", returning = "result")
    public void afterReturning(Notice notice, Object result) {
        try {
            // 获取当前登录用户ID
            Long userId = StpUtil.getLoginIdAsLong();

            // 解析 SpEL 表达式生成消息内容
            String content = parseSpel(notice.message(), result);

            // 构建消息对象
            UserNotice userNotice = new UserNotice();
            userNotice.setUserId(userId);
            userNotice.setCategory(notice.category());
            userNotice.setTitle(notice.title());
            userNotice.setContent(content);
            userNotice.setReadFlag(0); // 0未读
            userNotice.setDeleted(0);  // 0正常
            userNotice.setCreatedAt(LocalDateTime.now());

            // 插入消息
            userNoticeMapper.insert(userNotice);

            log.info("站内消息发送成功：userId={}, category={}, title={}, content={}",
                    userId, notice.category(), notice.title(), content);

        } catch (Exception e) {
            // 发送消息失败不影响主业务，仅记录日志
            log.error("发送站内消息失败", e);
        }
    }

    /**
     * 解析 SpEL 表达式
     *
     * @param template 模板字符串
     * @param result   方法返回值
     * @return 解析后的字符串
     */
    private String parseSpel(String template, Object result) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("result", result);

        // 使用模板解析模式，只解析 #{...} 中的表达式，其他部分（包括中文）作为普通文本
        Object value = parser.parseExpression(template, ParserContext.TEMPLATE_EXPRESSION).getValue(context);

        return value != null ? value.toString() : template;
    }
}
