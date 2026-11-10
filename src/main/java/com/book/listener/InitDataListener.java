package com.book.listener;


import com.book.entity.Book;
import com.book.entity.Chapter;
import com.book.entity.User;
import com.book.util.BookEnricher;
import com.book.util.TxtBookParser;
import com.book.util.UserStore;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 启动初始化数据监听器
 * 服务器启动时初始化图书集合、用户集合
 *
 * 所有图书统一从 WEB-INF/books 目录导入，元信息集中维护在 BOOK_META 中，
 * 该表的顺序决定图书 id（从 1 开始）。
 */
public class InitDataListener implements ServletContextListener {

    /** 图书元信息：书名 -> [作者, 分类, 简介, 所有者]，顺序决定图书 id */
    private static final Map<String, String[]> BOOK_META = new LinkedHashMap<>();

    static {
        BOOK_META.put("Java编程思想", new String[]{"Bruce Eckel", "计算机",
                "一本优秀的Java学习图书，全面介绍了Java语言的核心概念、面向对象编程思想以及实用技巧。", "钱多多"});
        BOOK_META.put("数据结构与算法", new String[]{"严薇敏", "计算机",
                "系统介绍常用数据结构和经典算法，包含大量实例和图解，适合初学者入门。", "李明"});
        BOOK_META.put("平凡的世界", new String[]{"路遥", "文学",
                "全景式展现中国当代城乡社会生活的长篇小说，激励了无数青年奋斗向上。", "王芳"});
        BOOK_META.put("活着", new String[]{"余华", "文学",
                "讲述一个人一生的苦难与坚韧，反映中国几十年社会变迁的经典之作。", "张伟"});
        BOOK_META.put("百年孤独", new String[]{"马尔克斯", "文学",
                "魔幻现实主义文学的代表作，描绘布恩迪亚家族七代人的传奇故事。", "刘娜"});
        BOOK_META.put("算法导论", new String[]{"Thomas H. Cormen", "计算机",
                "算法领域的经典教材，全面深入地介绍了各类算法的设计与分析方法。", "陈学"});
        BOOK_META.put("红楼梦", new String[]{"曹雪芹", "文学",
                "中国古典四大名著之首，以贾宝玉和林黛玉的爱情故事为主线，展现封建社会的兴衰。", "王芳"});
        BOOK_META.put("三体", new String[]{"刘慈欣", "科幻",
                "中国科幻文学里程碑式作品，讲述地球文明与三体文明的宇宙博弈。", "李明"});
        BOOK_META.put("围城", new String[]{"钱钟书", "文学",
                "以幽默讽刺的笔调描写知识分子的生活与心理，被誉为中国现代文学的经典。", "张伟"});
        BOOK_META.put("计算机网络", new String[]{"谢希仁", "计算机",
                "全面介绍计算机网络的基本原理、协议和应用，是计算机专业核心课程教材。", "陈学"});
        BOOK_META.put("银河系漫游指南", new String[]{"道格拉斯·亚当斯", "科幻",
                "一部荒诞幽默的科幻喜剧，讲述了地球被拆迁后主角在宇宙中的奇妙冒险。", "刘娜"});
        BOOK_META.put("经济学原理", new String[]{"曼昆", "经济",
                "全球最畅销的经济学教科书，以通俗易懂的方式阐述微观和宏观经济学核心原理。", "王芳"});
        BOOK_META.put("三国演义", new String[]{"罗贯中", "古典",
                "东汉末年群雄逐鹿，魏蜀吴三国鼎立的史诗巨著。", "馆藏"});
        BOOK_META.put("水浒传", new String[]{"施耐庵", "古典",
                "北宋末年一百零八位好汉梁山聚义的英雄传奇。", "馆藏"});
        BOOK_META.put("西游记", new String[]{"吴承恩", "古典",
                "唐僧师徒四人西天取经的神魔小说经典。", "馆藏"});
        BOOK_META.put("骆驼祥子", new String[]{"老舍", "文学",
                "老舍代表作，讲述旧北京人力车夫祥子三起三落的人生，写尽底层小人物的挣扎与沉沦。", "王芳"});
        BOOK_META.put("呐喊", new String[]{"鲁迅", "文学",
                "鲁迅第一部小说集，收录《狂人日记》《孔乙己》《阿Q正传》等名篇，剖开旧社会的病根。", "李明"});
        BOOK_META.put("边城", new String[]{"沈从文", "文学",
                "湘西边地的一段青春往事，翠翠与傩送的爱情在渡船与歌声中静静展开。", "刘娜"});
        BOOK_META.put("我们仨", new String[]{"杨绛", "文学",
                "杨绛回忆与钱锺书、钱瑗一家三口的相守与离散，平淡笔调里满是深情。", "张伟"});
        BOOK_META.put("白鹿原", new String[]{"陈忠实", "文学",
                "以白鹿原上白、鹿两大家族的兴衰，写尽关中乡村数十年的时代变迁。", "陈学"});
        BOOK_META.put("流浪地球", new String[]{"刘慈欣", "科幻",
                "太阳即将氦闪，人类给地球装上发动机，踏上漫长的流浪之旅。", "李明"});
        BOOK_META.put("球状闪电", new String[]{"刘慈欣", "科幻",
                "一场雷雨夜的奇异蓝光，引出对球状闪电的追寻与量子世界的惊心动魄。", "刘娜"});
        BOOK_META.put("海底两万里", new String[]{"儒勒·凡尔纳", "科幻",
                "跟随尼摩船长的鹦鹉螺号潜入深海，展开一场横跨大洋的奇幻航行。", "馆藏"});
        BOOK_META.put("论语", new String[]{"孔子及弟子", "古典",
                "儒家经典，记录孔子及其弟子的言行，是中国传统文化的重要源头。", "馆藏"});
        BOOK_META.put("史记", new String[]{"司马迁", "古典",
                "中国第一部纪传体通史，从五帝写到汉武，被誉为史家之绝唱。", "馆藏"});
        BOOK_META.put("代码大全", new String[]{"Steve McConnell", "计算机",
                "软件构建领域的经典，系统讲解编码、变量、控制结构与代码质量。", "钱多多"});
        BOOK_META.put("人月神话", new String[]{"Frederick P. Brooks", "计算机",
                "软件工程经典文集，讨论项目管理、团队协作与软件复杂度。", "钱多多"});
        BOOK_META.put("深入理解计算机系统", new String[]{"Randal E. Bryant", "计算机",
                "从程序员视角讲解计算机系统，涵盖数据表示、汇编、存储器与并发。", "陈学"});
        BOOK_META.put("国富论", new String[]{"亚当·斯密", "经济",
                "现代经济学奠基之作，论述分工、货币、价值与自由贸易。", "王芳"});
        BOOK_META.put("牛奶可乐经济学", new String[]{"罗伯特·弗兰克", "经济",
                "用经济学原理解释日常生活中的有趣现象，通俗易懂。", "张伟"});
        BOOK_META.put("万历十五年", new String[]{"黄仁宇", "历史",
                "以万历十五年这一平常年份为切口，剖析明代政治、制度与人物，开创大历史写法。", "陈学"});
        BOOK_META.put("全球通史", new String[]{"斯塔夫里阿诺斯", "历史",
                "从史前到当代的世界通史，以全球视角讲述人类文明的兴衰与交流。", "馆藏"});
        BOOK_META.put("苏菲的世界", new String[]{"乔斯坦·贾德", "哲学",
                "以小说形式串起西方哲学史，从苏格拉底到现代哲学，通俗好读。", "刘娜"});
        BOOK_META.put("沉思录", new String[]{"马可·奥勒留", "哲学",
                "古罗马皇帝的自我修养笔记，谈理性、克制与面对生死。", "张伟"});
        BOOK_META.put("思考，快与慢", new String[]{"丹尼尔·卡尼曼", "心理",
                "诺奖得主解释直觉与理性的两套思维系统，揭示人类判断中的偏见。", "王芳"});
        BOOK_META.put("乌合之众", new String[]{"古斯塔夫·勒庞", "心理",
                "群体心理学经典，分析人在群体中的情绪、盲从与行为特征。", "李明"});
        BOOK_META.put("艺术的故事", new String[]{"贡布里希", "艺术",
                "西方艺术史入门经典，以清晰叙述串起从洞窟壁画到现代艺术。", "刘娜"});
        BOOK_META.put("美的历程", new String[]{"李泽厚", "艺术",
                "从远古到明清，梳理中国美学与艺术精神的发展脉络。", "王芳"});
        BOOK_META.put("时间简史", new String[]{"史蒂芬·霍金", "科普",
                "面向大众的宇宙学经典，讲述大爆炸、黑洞与时间本质。", "陈学"});
        BOOK_META.put("万物简史", new String[]{"比尔·布莱森", "科普",
                "用幽默笔调讲述科学史与自然史，从宇宙起源到生命演化。", "李明"});
        BOOK_META.put("苏东坡传", new String[]{"林语堂", "传记",
                "林语堂笔下的苏轼，一生坎坷却旷达洒脱，是文人精神的写照。", "张伟"});
        BOOK_META.put("史蒂夫·乔布斯传", new String[]{"沃尔特·艾萨克森", "传记",
                "乔布斯唯一授权的传记，记录他的创业、离开、回归与产品哲学。", "钱多多"});
        BOOK_META.put("局外人", new String[]{"阿尔贝·加缪", "小说",
                "默尔索在母亲葬礼上没有流泪，后来在海滩上开枪杀人，审判关注的却是他的冷漠。", "刘娜"});
        BOOK_META.put("解忧杂货店", new String[]{"东野圭吾", "小说",
                "一间能回信的杂货店，把陌生人的烦恼串联成温暖的故事。", "王芳"});
        BOOK_META.put("挪威的森林", new String[]{"村上春树", "小说",
                "青春、爱情与失去，一段关于成长与孤独的记忆。", "张伟"});
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        //初始化在线人数为0
        context.setAttribute("onlineCount", 0);

        //初始化用户集合：尝试从文件加载，没有文件则创建空列表
        String realPath = context.getRealPath("/");
        List<User> userList = UserStore.load(realPath);
        if (userList.isEmpty()) {
            userList = new ArrayList<>();
        }
        context.setAttribute("userList", userList);

        //初始化图书集合：全部从 WEB-INF/books 导入
        List<Book> bookList = new ArrayList<>();
        loadBooks(realPath, bookList);
        context.setAttribute("bookList", bookList);

        // 启动后台线程，从豆瓣获取真实封面和简介（找不到封面时用 AI 生成），
        // 结果缓存到 WEB-INF/covers，之后的页面访问直接使用
        BookEnricher.enrichAll(bookList, context);
    }

    /**
     * 统一从 WEB-INF/books 导入全部图书：
     * 先按 BOOK_META 顺序分配 id（保证内置图书 id 稳定），
     * 目录中额外新增的 txt 按文件名排序追加到末尾。
     */
    private static void loadBooks(String realPath, List<Book> bookList) {
        File booksDir = new File(realPath, "WEB-INF" + File.separator + "books");
        Map<String, File> txtByName = new HashMap<>();
        File[] txtFiles = booksDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (txtFiles != null) {
            for (File txtFile : txtFiles) {
                String bookName = txtFile.getName().replaceAll("(?i)\\.txt$", "");
                txtByName.put(bookName, txtFile);
            }
        }

        long id = 1;
        for (Map.Entry<String, String[]> entry : BOOK_META.entrySet()) {
            File txtFile = txtByName.remove(entry.getKey());
            if (txtFile == null) {
                System.err.println("[InitData] 缺少图书文件: " + entry.getKey() + ".txt");
                continue;
            }
            addBook(bookList, id++, entry.getKey(), entry.getValue(), txtFile);
        }

        List<String> remaining = new ArrayList<>(txtByName.keySet());
        Collections.sort(remaining);
        for (String name : remaining) {
            addBook(bookList, id++, name, null, txtByName.get(name));
        }
    }

    private static void addBook(List<Book> bookList, long id, String name, String[] meta, File txtFile) {
        try {
            List<Chapter> chapters = TxtBookParser.parse(txtFile);
            if (chapters.isEmpty()) {
                return;
            }
            String author = meta != null ? meta[0] : "佚名";
            String category = meta != null ? meta[1] : "古典";
            String description = meta != null ? meta[2] : "导入的全本小说";
            String owner = meta != null && meta.length > 3 ? meta[3] : "馆藏";
            bookList.add(new Book(id, name, author, category, description, true, owner, chapters));
        } catch (IOException e) {
            System.err.println("[InitData] 导入图书失败: " + txtFile.getName() + " " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 服务器关闭时执行，这里不需要额外操作
    }
}
