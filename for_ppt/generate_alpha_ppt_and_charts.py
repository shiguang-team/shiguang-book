# -*- coding: utf-8 -*-
"""
拾光小组（校园二手书交易平台 shiguang-book）
Alpha 阶段博客配套图表（SVG + PNG）与答辩 PPT 一键生成脚本
"""

import os
import platform
import numpy as np
import matplotlib
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib import font_manager

from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

# Use non-interactive backend for headless rendering
matplotlib.use("Agg")

# ==============================================================================
# 0. 全局配色与中文字体自动适配（兼容 Windows / macOS / Linux）
# ==============================================================================
PNG_DIR = "alpha_charts_png"
SVG_DIR = "alpha_charts_svg"
PPTX_FILENAME = "拾光小组_Alpha阶段冲刺总结与答辩.pptx"

os.makedirs(PNG_DIR, exist_ok=True)
os.makedirs(SVG_DIR, exist_ok=True)


def configure_chinese_font():
    """自动检测系统可用的中文字体，彻底解决 matplotlib 中文乱码方框问题"""
    candidates = [
        "Microsoft YaHei", "SimHei", "DengXian",           # Windows
        "PingFang SC", "Heiti SC", "STHeiti", "Songti SC", # macOS
        "Noto Sans CJK SC", "WenQuanYi Micro Hei", "Arial Unicode MS"  # Linux
    ]
    available = {f.name for f in font_manager.fontManager.ttflist}
    chosen = None
    for c in candidates:
        if c in available:
            chosen = c
            break
    if chosen:
        plt.rcParams["font.sans-serif"] = [chosen, "DejaVu Sans"]
    else:
        plt.rcParams["font.sans-serif"] = candidates + ["DejaVu Sans"]
    plt.rcParams["axes.unicode_minus"] = False
    plt.rcParams["svg.fonttype"] = "none"  # 让 SVG 保留可编辑文本标签
    return chosen or "Microsoft YaHei"


CHINESE_FONT = configure_chinese_font()
print(f"[Info] 已启用中文字体: {CHINESE_FONT}")


def save_dual_format(fig, basename):
    """同时导出 300 DPI 的 PNG 和矢量 SVG"""
    png_path = os.path.join(PNG_DIR, f"{basename}.png")
    svg_path = os.path.join(SVG_DIR, f"{basename}.svg")
    fig.savefig(png_path, dpi=300, bbox_inches="tight", facecolor=fig.get_facecolor())
    fig.savefig(svg_path, format="svg", bbox_inches="tight", facecolor=fig.get_facecolor())
    plt.close(fig)
    print(f"  -> 已生成图表: {png_path} & {svg_path}")
    return png_path


# ==============================================================================
# 1. 生成图表 1：第 6 周 vs 第 7 周双周燃尽图对比（SVG + PNG）
# ==============================================================================
def make_chart_01_burndown():
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5.6), facecolor="#FFFFFF")
    fig.subplots_adjust(wspace=0.22, top=0.82, bottom=0.16)

    # --- 左图：第 6 周（Sprint 6）---
    days_w6 = ["10-26\n(计划会)", "10-27", "10-28", "10-29\n(站会)", "10-30", "10-31\n(评审)", "11-01"]
    ideal_w6 = [110, 91.7, 73.3, 55.0, 36.7, 18.3, 0]
    actual_w6 = [110, 106, 95, 78, 62, 38, 26]
    daily_w6 = [0, 4, 11, 17, 16, 24, 12]
    x = np.arange(len(days_w6))

    ax1.bar(x, daily_w6, width=0.42, color="#E2E8F0", label="当日完成（点）", zorder=2)
    ax1.plot(x, ideal_w6, "--", color="#64748B", linewidth=2, label="理想燃尽线", zorder=3)
    ax1.plot(x, actual_w6, "-o", color="#DC2626", linewidth=2.8, markersize=7, label="实际剩余（点）", zorder=4)
    ax1.fill_between(x, ideal_w6, actual_w6, color="#FEE2E2", alpha=0.45, zorder=1)

    for i, val in enumerate(actual_w6):
        ax1.annotate(f"{val}", (x[i], val + 3.5), ha="center", fontsize=9.5, fontweight="bold", color="#B91C1C")

    ax1.annotate("前两天前端等接口\n仅烧 4点 / 11点", xy=(1, 106), xytext=(1.2, 116),
                 arrowprops=dict(arrowstyle="->", color="#DC2626", lw=1.3),
                 fontsize=9, color="#991B1B", fontweight="bold",
                 bbox=dict(boxstyle="round,pad=0.3", fc="#FEF2F2", ec="#FCA5A5"))
    ax1.annotate("周四站会调序后陡降\n遗留 26 点 (完成率 76.4%)", xy=(6, 26), xytext=(3.5, 10),
                 arrowprops=dict(arrowstyle="->", color="#DC2626", lw=1.3),
                 fontsize=9, color="#991B1B", fontweight="bold",
                 bbox=dict(boxstyle="round,pad=0.3", fc="#FEF2F2", ec="#FCA5A5"))

    ax1.set_title("第 6 周 Sprint 6 燃尽图（共 110 点，完成 76.4%）\n特征：“前平后陡”，暴露前后端前置依赖阻塞",
                  fontsize=12, fontweight="bold", color="#1E293B", pad=12)
    ax1.set_xticks(x)
    ax1.set_xticklabels(days_w6, fontsize=9)
    ax1.set_ylim(0, 132)
    ax1.set_ylabel("剩余故事点（Story Points）", fontsize=10, fontweight="bold")
    ax1.grid(axis="y", linestyle=":", alpha=0.6)
    ax1.spines["top"].set_visible(False)
    ax1.spines["right"].set_visible(False)
    ax1.legend(loc="upper right", frameon=True, fontsize=8.5)

    # --- 右图：第 7 周（Sprint 7）---
    days_w7 = ["11-02\n(契约冻结)", "11-03", "11-04", "11-05\n(站会)", "11-06\n(功能冻结)", "11-07\n(评审)", "11-08"]
    ideal_w7 = [64.0, 53.3, 42.7, 32.0, 21.3, 10.7, 0.0]
    actual_w7 = [58, 47, 34, 22, 8, 2, 0]
    daily_w7 = [6, 11, 13, 12, 14, 6, 2]

    ax2.bar(x, daily_w7, width=0.42, color="#DCFCE7", label="当日完成（点）", zorder=2)
    ax2.plot(x, ideal_w7, "--", color="#64748B", linewidth=2, label="理想燃尽线", zorder=3)
    ax2.plot(x, actual_w7, "-o", color="#16A34A", linewidth=2.8, markersize=7, label="实际剩余（点）", zorder=4)
    ax2.fill_between(x, actual_w7, ideal_w7, color="#DCFCE7", alpha=0.4, zorder=1)

    for i, val in enumerate(actual_w7):
        ax2.annotate(f"{val}", (x[i], val + 2.2), ha="center", fontsize=9.5, fontweight="bold", color="#15803D")

    ax2.annotate("周一 11:30 冻结接口契约\n首日即并行烧掉 6 点", xy=(0, 58), xytext=(0.7, 63),
                 arrowprops=dict(arrowstyle="->", color="#16A34A", lw=1.3),
                 fontsize=9, color="#166534", fontweight="bold",
                 bbox=dict(boxstyle="round,pad=0.3", fc="#F0FDF4", ec="#86EFAC"))
    ax2.annotate("周五 22:00 功能冻结仅剩 8 点\n周末全量回归，64 点 100% 烧完", xy=(4, 8), xytext=(2.1, 22),
                 arrowprops=dict(arrowstyle="->", color="#16A34A", lw=1.3),
                 fontsize=9, color="#166534", fontweight="bold",
                 bbox=dict(boxstyle="round,pad=0.3", fc="#F0FDF4", ec="#86EFAC"))

    ax2.set_title("第 7 周 Sprint 7 燃尽图（共 64 点，完成 100%）\n特征：“匀速贴线”，接口契约先行消除等待期",
                  fontsize=12, fontweight="bold", color="#1E293B", pad=12)
    ax2.set_xticks(x)
    ax2.set_xticklabels(days_w7, fontsize=9)
    ax2.set_ylim(0, 76)
    ax2.set_ylabel("剩余故事点（Story Points）", fontsize=10, fontweight="bold")
    ax2.grid(axis="y", linestyle=":", alpha=0.6)
    ax2.spines["top"].set_visible(False)
    ax2.spines["right"].set_visible(False)
    ax2.legend(loc="upper right", frameon=True, fontsize=8.5)

    fig.suptitle("拾光小组 Alpha 冲刺双周燃尽图对比（累计完成 148 点，遗留 0 点）",
                 fontsize=14.5, fontweight="bold", color="#0F172A", y=0.96)
    return save_dual_format(fig, "01_burndown_comparison")


# ==============================================================================
# 2. 生成图表 2：五人工作量、代码签入（58次）与 Issue 闭环（32个）全景图
# ==============================================================================
def make_chart_02_workload():
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5.4), facecolor="#FFFFFF",
                                   gridspec_kw={"width_ratios": [1.35, 1]})
    fig.subplots_adjust(wspace=0.25, top=0.82, bottom=0.15)

    members = [
        "李科霖\n(组长/前端A)",
        "李卓轩\n(前端B)",
        "张佳鹏\n(后端A)",
        "杨昀博\n(后端B)",
        "陈意\n(测试/部署)"
    ]
    pts_w6 = np.array([17, 17, 20, 19, 11])
    pts_w7 = np.array([8, 16, 12, 13, 15])
    commits_w6 = np.array([6, 5, 7, 5, 7])
    commits_w7 = np.array([5, 6, 6, 5, 6])

    x = np.arange(len(members))
    w = 0.34

    # 左轴：故事点堆叠柱状图 + 提交次数柱状图
    b1 = ax1.bar(x - w/2, pts_w6, width=w, color="#2563EB", label="第 6 周完成点数 (共 84 点)")
    b2 = ax1.bar(x - w/2, pts_w7, width=w, bottom=pts_w6, color="#60A5FA", label="第 7 周完成点数 (共 64 点)")

    b3 = ax1.bar(x + w/2, commits_w6, width=w, color="#059669", label="第 6 周提交次数 (共 30 次)")
    b4 = ax1.bar(x + w/2, commits_w7, width=w, bottom=commits_w6, color="#34D399", label="第 7 周提交次数 (共 28 次)")

    for i in range(len(members)):
        total_p = pts_w6[i] + pts_w7[i]
        total_c = commits_w6[i] + commits_w7[i]
        ax1.text(x[i] - w/2, total_p + 0.8, f"{total_p}点", ha="center", fontsize=9.5, fontweight="bold", color="#1D4ED8")
        ax1.text(x[i] + w/2, total_c + 0.8, f"{total_c}次", ha="center", fontsize=9.5, fontweight="bold", color="#047857")

    ax1.set_title("各成员两周故事点产出（148点）与 Git 提交分布（58次）", fontsize=12, fontweight="bold", color="#1E293B", pad=12)
    ax1.set_xticks(x)
    ax1.set_xticklabels(members, fontsize=9.5, fontweight="bold")
    ax1.set_ylim(0, 40)
    ax1.set_ylabel("故事点数 / 提交次数", fontsize=10, fontweight="bold")
    ax1.grid(axis="y", linestyle=":", alpha=0.6)
    ax1.spines["top"].set_visible(False)
    ax1.spines["right"].set_visible(False)
    ax1.legend(loc="upper right", ncol=2, fontsize=8.5, frameon=True)

    # 右图：32 个 Issue 闭环构成与状态分布
    categories = ["第 6 周当周完成\n(15 个 Issue)", "第 6 周顺延至第 7 周完成\n(8 个 Issue)", "第 7 周新增并完成\n(9 个 Issue)"]
    counts = [15, 8, 9]
    colors = ["#2563EB", "#F59E0B", "#10B981"]
    wedges, texts, autotexts = ax2.pie(
        counts,
        labels=categories,
        autopct=lambda pct: f"{pct:.1f}%\n({int(round(pct/100.*sum(counts)))}个)",
        startangle=140,
        colors=colors,
        pctdistance=0.65,
        wedgeprops=dict(width=0.45, edgecolor="white", linewidth=2),
        textprops=dict(fontsize=9.5, fontweight="bold", color="#1E293B")
    )
    for autotext in autotexts:
        autotext.set_color("white")
        autotext.set_fontsize(9)

    ax2.text(0, 0, "累计 32 个\nIssue\n100% 闭环\n(0 遗留)", ha="center", va="center",
             fontsize=10.5, fontweight="bold", color="#0F172A")
    ax2.set_title("Alpha 阶段 32 个 Issue 闭环生命周期分布", fontsize=12, fontweight="bold", color="#1E293B", pad=12)

    fig.suptitle("拾光小组团队贡献度与 Issue 看板闭环统计（全员 100% 关联 Issue 签入）",
                 fontsize=14.5, fontweight="bold", color="#0F172A", y=0.96)
    return save_dual_format(fig, "02_team_workload_issues")


# ==============================================================================
# 3. 生成图表 3：质量与性能三联看板（46条接口用例 + 14个Bug收敛 + 性能调优）
# ==============================================================================
def make_chart_03_testing_performance():
    fig, (ax1, ax2, ax3) = plt.subplots(1, 3, figsize=(15, 5.2), facecolor="#FFFFFF")
    fig.subplots_adjust(wspace=0.28, top=0.80, bottom=0.16)

    # --- Panel 1: Apifox 46 条接口测试用例分布 ---
    modules = ["书籍与搜索\n(#11/#13/#8)", "用户与鉴权\n(#6/#7/#29)", "留言与未读\n(#15/#27)", "收藏夹模块\n(#26)", "文件上传\n(#12)"]
    case_counts = [16, 10, 9, 7, 4]
    y_pos = np.arange(len(modules))

    bars = ax1.barh(y_pos, case_counts, color=["#1D4ED8", "#2563EB", "#3B82F6", "#60A5FA", "#93C5FD"], height=0.55)
    ax1.invert_yaxis()
    for bar, cnt in zip(bars, case_counts):
        ax1.text(cnt + 0.4, bar.get_y() + bar.get_height()/2, f"{cnt} 条 (100%通过)",
                 va="center", fontsize=9, fontweight="bold", color="#1E3A8A")

    ax1.set_xlim(0, 23)
    ax1.set_yticks(y_pos)
    ax1.set_yticklabels(modules, fontsize=9, fontweight="bold")
    ax1.set_xlabel("Apifox 用例数量（共 46 条全绿）", fontsize=9.5, fontweight="bold")
    ax1.set_title("① 接口测试覆盖（46 条 100% 通过）\n含越权/429锁定/并发幂等/409边界", fontsize=11, fontweight="bold", pad=10)
    ax1.grid(axis="x", linestyle=":", alpha=0.6)
    ax1.spines["top"].set_visible(False)
    ax1.spines["right"].set_visible(False)

    # --- Panel 2: 14 个缺陷严重程度与双周清零趋势 ---
    severities = ["高危 (High)\nBUG-01/05/06/09/13", "中危 (Medium)\nBUG-02/03/04/10/11/14", "低危 (Low)\nBUG-07/08/12"]
    # High: 01, 05, 06, 09 (W6=4) + 13 (W7=1) -> Wait: let's match exact table:
    # W6 Fixed: 7, W6 Carryover: 2 (BUG-04 Med, BUG-07 Low). W7 New: 5 (BUG-10 Med, 11 Med, 12 Low, 13 High, 14 Med)
    stages = ["第 6 周发现\n(共 9 个)", "第 6 周末遗留\n(顺延 2 个)", "第 7 周新测出\n(新增 5 个)", "Alpha 终态遗留\n(14 个全清零)"]
    bug_nums = [9, 2, 5, 0]
    bar_colors = ["#F59E0B", "#EF4444", "#3B82F6", "#10B981"]

    b_bugs = ax2.bar( np.arange(len(stages)), bug_nums, width=0.48, color=bar_colors)
    for idx, val in enumerate(bug_nums):
        label_txt = f"{val} 个" if val > 0 else "0 遗留 (100%闭环)"
        ax2.text(idx, val + 0.4, label_txt, ha="center", fontsize=9.5, fontweight="bold",
                 color="#047857" if val == 0 else "#1E293B")

    ax2.set_xticks(np.arange(len(stages)))
    ax2.set_xticklabels(stages, fontsize=8.8, fontweight="bold")
    ax2.set_ylim(0, 11.5)
    ax2.set_ylabel("缺陷数量（个）", fontsize=9.5, fontweight="bold")
    ax2.set_title("② 缺陷跟踪与收敛（累计 14 个清零）\n周五 22:00 功能冻结保障全量回归", fontsize=11, fontweight="bold", pad=10)
    ax2.grid(axis="y", linestyle=":", alpha=0.6)
    ax2.spines["top"].set_visible(False)
    ax2.spines["right"].set_visible(False)

    # --- Panel 3: 5000条数据慢查询优化 & 2C2G 内存控制 ---
    metrics = ["5000条数据多条件查询\n耗时 (ms)", "2C2G 学生机整机\n内存占用率 (%)"]
    before_vals = [68, 98]
    after_vals = [4, 62]
    x_m = np.arange(len(metrics))
    w_m = 0.32

    ax3.bar(x_m - w_m/2, before_vals, width=w_m, color="#EF4444", label="优化前 (单列索引 / 默认内存)")
    ax3.bar(x_m + w_m/2, after_vals, width=w_m, color="#10B981", label="优化后 (联合索引 / JVM+Swap限制)")

    ax3.text(0 - w_m/2, 68 + 2, "68 ms\n(filesort)", ha="center", fontsize=8.5, fontweight="bold", color="#B91C1C")
    ax3.text(0 + w_m/2, 4 + 2, "4 ms\n(提速 17倍)", ha="center", fontsize=8.5, fontweight="bold", color="#047857")
    ax3.text(1 - w_m/2, 98 + 2, "98%+ (OOM)\n进程被杀", ha="center", fontsize=8.5, fontweight="bold", color="#B91C1C")
    ax3.text(1 + w_m/2, 62 + 2, "62% 稳定\n零外部容器", ha="center", fontsize=8.5, fontweight="bold", color="#047857")

    ax3.set_xticks(x_m)
    ax3.set_xticklabels(metrics, fontsize=9, fontweight="bold")
    ax3.set_ylim(0, 118)
    ax3.set_ylabel("耗时 (ms) / 内存占比 (%)", fontsize=9.5, fontweight="bold")
    ax3.set_title("③ 慢查询索引优化与 2C2G 内存调优\n联合索引消除 filesort + 轻量黑名单", fontsize=11, fontweight="bold", pad=10)
    ax3.grid(axis="y", linestyle=":", alpha=0.6)
    ax3.spines["top"].set_visible(False)
    ax3.spines["right"].set_visible(False)
    ax3.legend(loc="upper left", fontsize=8.2, frameon=True)

    fig.suptitle("拾光小组 Alpha 阶段质量保障体系（46 条接口用例 + 12 条 JUnit 单测覆盖率 82% + 14 个缺陷清零）",
                 fontsize=14, fontweight="bold", color="#0F172A", y=0.96)
    return save_dual_format(fig, "03_testing_and_performance")


# ==============================================================================
# 4. 生成图表 4：14 个缺陷（BUG-01 ~ BUG-14）高清可视化表格（SVG + PNG）
# ==============================================================================
def make_chart_04_buglist_table():
    fig, ax = plt.subplots(figsize=(15, 7.2), facecolor="#FFFFFF")
    ax.axis("off")

    headers = ["Bug 编号", "周次", "发现人", "严重度", "缺陷现象简述", "根本原因与工程修复方案", "状态"]
    rows = [
        ["BUG-01", "第6周", "陈意", "高", "登录态失效或刷新页面触发 401 死循环白屏", "拦截器只清 Token 不直接跳路由，统一交由路由守卫判断", "已修复"],
        ["BUG-02", "第6周", "李卓轩", "中", "列表翻到第 2 页仍显示第 1 页数据且未回顶", "修正分页请求参数同步，并在 onPage 补 window.scrollTo(0,0)", "已修复"],
        ["BUG-03", "第6周", "杨昀博", "中", "上传 2.3MB 教材封面返回 413 且前端无提示", "Nginx 配置 client_max_body_size 4m + 前端 2MB 预校验", "已修复"],
        ["BUG-04", "第6→7周", "陈意", "中", "价格区间任意取值或留空查不到结果/报 SQL 错", "BookQuery 改用 BigDecimal + ge/le，前端提交前清理空串", "已修复"],
        ["BUG-05", "第6周", "杨昀博", "高", "站内留言超过 200 字直接触发异常返回 500", "DTO 加 @Size(max=200) 校验 + GlobalExceptionHandler 兜底", "已修复"],
        ["BUG-06", "第6周", "陈意", "高", "手机号前后带空格也能注册，产生重复账号", "入库前统一执行 String.trim() + 数据库手机号唯一索引", "已修复"],
        ["BUG-07", "第6→7周", "李科霖", "低", "个人中心“我发布的书籍”分页切换未生效", "Profile.vue 页码切换事件补调 loadMyBooks() 重新拉取", "已修复"],
        ["BUG-08", "第6周", "李卓轩", "低", "卖家默认头像与容器内上传图片访问全 404", "Docker 命名卷挂载与 WebMvcConfig 统一映射 /files/** 路径", "已修复"],
        ["BUG-09", "第6周", "陈意", "高", "2C2G 服务器跑一段时间后 Java 进程被 OOM 杀掉", "加 2G swap + JVM 限 -Xmx512m + MySQL buffer pool 降为 128M", "已修复"],
        ["BUG-10", "第7周", "陈意", "中", "详情页快速双击“收藏”触发唯一索引冲突报 500", "前端星标按钮加 loading 防抖锁 + 后端捕获冲突作幂等返回", "已修复"],
        ["BUG-11", "第7周", "李卓轩", "中", "从“编辑书籍”切到“发布书籍”表单残留旧书数据", "watch 监听 route.query.id 变化，切新建时主动调 resetFields()", "已修复"],
        ["BUG-12", "第7周", "陈意", "低", "iPhone SE (375px) 下分页条与弹窗撑破屏幕溢屏", "移动端动态启用 small 分页，弹窗宽度设为 min(92vw, 500px)", "已修复"],
        ["BUG-13", "第7周", "陈意", "高", "卖家删书后，收藏该书的买家打开收藏夹报 500 空指针", "FavoriteService 判空兜底，展示“【该书籍已被卖家删除】”灰标", "已修复"],
        ["BUG-14", "第7周", "陈意", "中", "执行演示重置 SQL 后，12 本内置教材封面图全 404", "12 张封面入库 deploy/demo-assets/，reset-demo.sh 自动 docker cp", "已修复"],
    ]

    col_widths = [0.075, 0.065, 0.065, 0.055, 0.30, 0.365, 0.075]
    table = ax.table(
        cellText=rows,
        colLabels=headers,
        colWidths=col_widths,
        loc="center",
        cellLoc="left"
    )
    table.auto_set_font_size(False)
    table.set_fontsize(8.8)
    table.scale(1.0, 1.52)

    for (row_idx, col_idx), cell in table.get_celld().items():
        cell.set_edgecolor("#CBD5E1")
        cell.set_linewidth(0.8)
        if row_idx == 0:
            cell.set_facecolor("#1E3A8A")
            cell.get_text().set_color("white")
            cell.get_text().set_weight("bold")
            cell.get_text().set_ha("center")
        else:
            bg = "#F8FAFC" if row_idx % 2 == 1 else "#FFFFFF"
            cell.set_facecolor(bg)
            if col_idx in (0, 1, 2, 3, 6):
                cell.get_text().set_ha("center")
            if col_idx == 0:
                cell.get_text().set_weight("bold")
                cell.get_text().set_color("#1E293B")
            elif col_idx == 3:
                sev = rows[row_idx - 1][3]
                cell.get_text().set_weight("bold")
                if sev == "高":
                    cell.set_facecolor("#FEE2E2")
                    cell.get_text().set_color("#B91C1C")
                elif sev == "中":
                    cell.set_facecolor("#FEF3C7")
                    cell.get_text().set_color("#B45309")
                else:
                    cell.set_facecolor("#E0F2FE")
                    cell.get_text().set_color("#0369A1")
            elif col_idx == 6:
                cell.set_facecolor("#DCFCE7")
                cell.get_text().set_color("#15803D")
                cell.get_text().set_weight("bold")

    ax.set_title("拾光小组 Alpha 阶段缺陷跟踪总表（BUG-01 ～ BUG-14，累计 14 项 100% 闭环清零）",
                 fontsize=13.5, fontweight="bold", color="#0F172A", pad=14)
    return save_dual_format(fig, "04_buglist_visual_table")


# ==============================================================================
# 5. 生成图表 5：四端兼容性测试矩阵 & Alpha 出口条件达成表（SVG + PNG）
# ==============================================================================
def make_chart_05_compatibility_matrix():
    fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(14, 6.2), facecolor="#FFFFFF",
                                   gridspec_kw={"height_ratios": [1.15, 1]})
    fig.subplots_adjust(hspace=0.35, top=0.90, bottom=0.05)

    # 上表：4 端兼容性测试矩阵
    ax1.axis("off")
    h1 = ["测试终端 / 浏览器", "分辨率断点", "首页卡片流与分页条", "筛选栏与搜索框", "书籍详情与留言区", "弹窗与表单上传", "结论"]
    r1 = [
        ["Win 11 + Chrome 130", "1920×1080", "多列 Grid (minmax 210px)，完整分页正常", "横向平铺展开，多条件联动流畅", "左图右文双栏，卖家信息一次回填", "500px 居中弹窗，星级评分正常", "通过"],
        ["macOS + Safari 17", "1440×900", "网格与字体渲染正常，翻页自动平滑回顶", "下拉框与价格区间输入对齐无错位", "封面比例一致，倒序留言列表正常", "二次确认弹窗与路由回跳正常", "通过"],
        ["iOS 17 + Safari (iPhone SE)", "375×667", "自动切手机双列流，small 精简分页无溢屏", "自动折叠进 el-collapse，省首屏空间", "响应式切换为“上图下文”单栏布局", "弹窗宽度 min(92vw,500px) 不贴边", "通过"],
        ["Android 14 + 微信内置浏览器", "414×896", "扫码直连顺畅，双列图文清晰，触控正常", "折叠面板展开顺滑，软键盘不遮挡", "星标收藏防抖、留言与红点轮询正常", "手机相册选图上传(≤2MB)回填正常", "通过"],
    ]
    cw1 = [0.16, 0.09, 0.20, 0.18, 0.18, 0.13, 0.06]
    t1 = ax1.table(cellText=r1, colLabels=h1, colWidths=cw1, loc="center", cellLoc="left")
    t1.auto_set_font_size(False)
    t1.set_fontsize(8.8)
    t1.scale(1.0, 1.55)
    for (ri, ci), cell in t1.get_celld().items():
        cell.set_edgecolor("#CBD5E1")
        if ri == 0:
            cell.set_facecolor("#0F172A")
            cell.get_text().set_color("white")
            cell.get_text().set_weight("bold")
            cell.get_text().set_ha("center")
        else:
            cell.set_facecolor("#F8FAFC" if ri % 2 == 1 else "#FFFFFF")
            if ci in (0, 1, 6):
                cell.get_text().set_ha("center")
            if ci == 0:
                cell.get_text().set_weight("bold")
            if ci == 6:
                cell.set_facecolor("#DCFCE7")
                cell.get_text().set_color("#15803D")
                cell.get_text().set_weight("bold")
    ax1.set_title("① 多端真机兼容性测试矩阵（PC 宽屏 + Mac + iOS 小屏 + Android 微信扫码全覆盖）",
                  fontsize=12, fontweight="bold", color="#0F172A", pad=8)

    # 下表：第 6 周风险解除与 Alpha 版本出口条件达成表
    ax2.axis("off")
    h2 = ["维度 / 风险项", "第 6 周暴露的短板与风险", "第 7 周工程落地与应对方案", "量化验收指标（出口条件）", "达成状态"]
    r2 = [
        ["鉴权安全与注销", "JWT 无状态，退出登录仅前端删 Token", "落地 TokenBlacklistService (内存 Map + 每小时定时清扫过期项)", "注销即刻作废，持旧 Token 访问 100% 返 401", "已解除 / 达成"],
        ["移动端真机体验", "移动端适配未开始 (#21)，小屏分页溢屏", "完成 375/414px 响应式双列流、折叠筛选栏与 small 分页适配", "PC 与手机主流浏览器排版正常，0 横向滚动条", "已解除 / 达成"],
        ["多条件搜索性能", "搜索走单列索引，数据量大时出现 filesort", "建立 idx_del_status_cat_created 联合索引，遵循最左前缀原则", "5000 条数据下 EXPLAIN 消除 filesort，耗时 68ms→4ms", "已解除 / 达成"],
        ["2C2G 部署与演示", "单台 2C2G 学生机曾发 OOM；演示易遇脏数据", "加 2G swap + 限 -Xmx512m；编写 reset-demo.sh 一键重置脚本", "内存稳定 62%，14 个 Bug 清零，5 分钟一键重置演示", "已受控 / 达成"],
    ]
    cw2 = [0.12, 0.22, 0.30, 0.26, 0.10]
    t2 = ax2.table(cellText=r2, colLabels=h2, colWidths=cw2, loc="center", cellLoc="left")
    t2.auto_set_font_size(False)
    t2.set_fontsize(8.8)
    t2.scale(1.0, 1.55)
    for (ri, ci), cell in t2.get_celld().items():
        cell.set_edgecolor("#CBD5E1")
        if ri == 0:
            cell.set_facecolor("#1E3A8A")
            cell.get_text().set_color("white")
            cell.get_text().set_weight("bold")
            cell.get_text().set_ha("center")
        else:
            cell.set_facecolor("#F8FAFC" if ri % 2 == 1 else "#FFFFFF")
            if ci in (0, 4):
                cell.get_text().set_ha("center")
                cell.get_text().set_weight("bold")
            if ci == 4:
                cell.set_facecolor("#DCFCE7")
                cell.get_text().set_color("#15803D")
    ax2.set_title("② 风险收敛与 Alpha 正式版（v0.2）四条可量化出口条件核验表",
                  fontsize=12, fontweight="bold", color="#0F172A", pad=8)

    return save_dual_format(fig, "05_compatibility_exit_table")


# ==============================================================================
# 6. PPTX 辅助绘制函数（16:9 宽屏现代化卡片式排版）
# ==============================================================================
COLOR_NAVY = RGBColor(15, 23, 42)       # #0F172A 深蓝黑标题
COLOR_BLUE = RGBColor(37, 99, 235)      # #2563EB 主题科技蓝
COLOR_LIGHT_BG = RGBColor(248, 250, 252)# #F8FAFC 浅灰底色
COLOR_WHITE = RGBColor(255, 255, 255)
COLOR_BORDER = RGBColor(203, 213, 225)  # #CBD5E1 边框灰
COLOR_TEXT = RGBColor(30, 41, 59)       # #1E293B 正文深灰
COLOR_MUTED = RGBColor(100, 116, 139)   # #64748B 次要文字
COLOR_GREEN = RGBColor(22, 163, 74)     # #16A34A 成功绿
COLOR_AMBER = RGBColor(217, 119, 6)     # #D97706 警示橙


def set_slide_background(slide, color=COLOR_LIGHT_BG):
    bg = slide.background
    fill = bg.fill
    fill.solid()
    fill.fore_color.rgb = color


def add_header_banner(slide, title_text, subtitle_text, page_num, total_pages=12):
    """为内容页添加统一的顶部深色导航条与页码"""
    banner = slide.shapes.add_shape(
        MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(13.333), Inches(0.95)
    )
    banner.fill.solid()
    banner.fill.fore_color.rgb = COLOR_NAVY
    banner.line.fill.background()

    # 左侧蓝色装饰竖条
    accent = slide.shapes.add_shape(
        MSO_SHAPE.RECTANGLE, Inches(0.45), Inches(0.18), Inches(0.10), Inches(0.58)
    )
    accent.fill.solid()
    accent.fill.fore_color.rgb = COLOR_BLUE
    accent.line.fill.background()

    # 主标题与副标题
    tb = slide.shapes.add_textbox(Inches(0.68), Inches(0.10), Inches(10.8), Inches(0.78))
    tf = tb.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = title_text
    p.font.size = Pt(22)
    p.font.bold = True
    p.font.color.rgb = COLOR_WHITE
    p.font.name = "Microsoft YaHei"

    p2 = tf.add_paragraph()
    p2.text = subtitle_text
    p2.font.size = Pt(11)
    p2.font.color.rgb = RGBColor(148, 163, 184)
    p2.font.name = "Microsoft YaHei"

    # 右侧页码
    pg_box = slide.shapes.add_textbox(Inches(11.6), Inches(0.25), Inches(1.3), Inches(0.45))
    pg_tf = pg_box.text_frame
    pg_p = pg_tf.paragraphs[0]
    pg_p.alignment = PP_ALIGN.RIGHT
    pg_p.text = f"{page_num:02d} / {total_pages:02d}"
    pg_p.font.size = Pt(13)
    pg_p.font.bold = True
    pg_p.font.color.rgb = RGBColor(96, 165, 250)
    pg_p.font.name = "Microsoft YaHei"


def add_card(slide, left, top, width, height, title=None, bullets=None,
             header_color=COLOR_BLUE, bg_color=COLOR_WHITE, font_size=12):
    """绘制带顶部色带的现代化圆角信息卡片"""
    card = slide.shapes.add_shape(
        MSO_SHAPE.ROUNDED_RECTANGLE, Inches(left), Inches(top), Inches(width), Inches(height)
    )
    card.adjustments[0] = 0.02
    card.fill.solid()
    card.fill.fore_color.rgb = bg_color
    card.line.color.rgb = COLOR_BORDER
    card.line.width = Pt(1)

    # 顶部细色条
    bar = slide.shapes.add_shape(
        MSO_SHAPE.RECTANGLE, Inches(left), Inches(top), Inches(width), Inches(0.08)
    )
    bar.fill.solid()
    bar.fill.fore_color.rgb = header_color
    bar.line.fill.background()

    tb = slide.shapes.add_textbox(Inches(left + 0.16), Inches(top + 0.14), Inches(width - 0.32), Inches(height - 0.22))
    tf = tb.text_frame
    tf.word_wrap = True

    first = True
    if title:
        p = tf.paragraphs[0]
        first = False
        p.text = title
        p.font.size = Pt(font_size + 2)
        p.font.bold = True
        p.font.color.rgb = COLOR_NAVY
        p.font.name = "Microsoft YaHei"
        p.space_after = Pt(6)

    if bullets:
        for b in bullets:
            p = tf.paragraphs[0] if first else tf.add_paragraph()
            first = False
            p.text = f"• {b}"
            p.font.size = Pt(font_size)
            p.font.color.rgb = COLOR_TEXT
            p.font.name = "Microsoft YaHei"
            p.space_after = Pt(4)
    return card


def add_notes(slide, text):
    notes_slide = slide.notes_slide
    tf = notes_slide.notes_text_frame
    tf.text = text


# ==============================================================================
# 7. 组装 12 页《拾光小组 Alpha 阶段冲刺总结与答辩 PPT》
# ==============================================================================
def build_presentation(chart_paths):
    prs = Presentation()
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank_layout = prs.slide_layouts[6]

    # --------------------------------------------------------------------------
    # Slide 1: 封面页
    # --------------------------------------------------------------------------
    s1 = prs.slides.add_slide(blank_layout)
    set_slide_background(s1, COLOR_NAVY)

    # 装饰背景块
    deco = s1.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(0.35), Inches(7.5))
    deco.fill.solid()
    deco.fill.fore_color.rgb = COLOR_BLUE
    deco.line.fill.background()

    tb1 = s1.shapes.add_textbox(Inches(1.0), Inches(1.1), Inches(11.3), Inches(3.2))
    tf1 = tb1.text_frame
    tf1.word_wrap = True

    p = tf1.paragraphs[0]
    p.text = "软件工程 Alpha 阶段冲刺总结与答辩汇报"
    p.font.size = Pt(18)
    p.font.bold = True
    p.font.color.rgb = RGBColor(96, 165, 250)
    p.font.name = "Microsoft YaHei"
    p.space_after = Pt(12)

    p2 = tf1.add_paragraph()
    p2.text = "校园二手书交易平台（shiguang-book）"
    p2.font.size = Pt(40)
    p2.font.bold = True
    p2.font.color.rgb = COLOR_WHITE
    p2.font.name = "Microsoft YaHei"
    p2.space_after = Pt(14)

    p3 = tf1.add_paragraph()
    p3.text = "从 v0.1 主干跑通到 v0.2 正式试用的两周敏捷冲刺（Scrum Meeting · 测试报告 · 软工方法总结）"
    p3.font.size = Pt(16)
    p3.font.color.rgb = RGBColor(203, 213, 225)
    p3.font.name = "Microsoft YaHei"

    # 封面底部信息卡片
    info_cards = [
        ("团队成员（拾光小组 5 人）", [
            "李科霖（组长 / 前端 A：工程骨架、个人中心、未读提醒）",
            "李卓轩（前端 B：列表、搜索筛选、详情、发布、移动适配）",
            "张佳鹏（后端 A：数据库、JWT鉴权、收藏、黑名单、单测）",
            "杨昀博（后端 B：书籍 CRUD、上传、留言、索引优化、重置）",
            "陈意（测试 / 部署：Docker、46条用例、14个Bug、测试报告）"
        ]),
        ("核心技术栈与工程交付指标", [
            "后端：Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 + MySQL 8 + JWT",
            "前端：Vue 3 + Vite 5 + Element Plus  |  部署：Docker + Nginx",
            "冲刺周期：Sprint 6 (10.26~11.01) & Sprint 7 (11.02~11.08)",
            "研发规模：58 次 Commit · 32 个 Issue 闭环 · 148 故事点完成",
            "质量指标：46 条接口用例全绿 · 12 条单测 · 14 个缺陷清零"
        ])
    ]
    for idx, (ctitle, cbullets) in enumerate(info_cards):
        add_card(s1, left=1.0 + idx * 5.75, top=4.25, width=5.45, height=2.55,
                 title=ctitle, bullets=cbullets, header_color=COLOR_BLUE, font_size=11)

    add_notes(s1, "各位老师、助教好！我是拾光小组组长李科霖。今天由我代表我们五人团队，汇报《校园二手书交易平台》在 Alpha 阶段第 6、7 周的 Scrum 冲刺成果、测试报告以及我们习得的软件工程方法。")

    # --------------------------------------------------------------------------
    # Slide 2: 目录与核心成果 KPI 仪表盘
    # --------------------------------------------------------------------------
    s2 = prs.slides.add_slide(blank_layout)
    set_slide_background(s2)
    add_header_banner(s2, "一、Alpha 阶段博客导航与核心成果总览",
                      "对应博客目录：一、Scrum Meeting（第6/7周） | 二、Alpha 阶段测试报告 | 三、习得的软工原理/方法/技能", 2)

    kpis = [
        ("148 点", "累计完成故事点", "第6周 84点 + 第7周 64点\n终态剩余 0 点（100% 烧完）", COLOR_BLUE),
        ("58 次", "Git 规范化签入", "第6周 30次 + 第7周 28次\n100% 携带 Issue 编号可溯源", COLOR_GREEN),
        ("32 / 32", "GitHub Issue 闭环", "第6周关15个 + 第7周关17个\n看板全部移入 Closed 列", COLOR_BLUE),
        ("46 条", "Apifox 接口用例全绿", "覆盖 5 大模块 + 12 条 JUnit 单测\n核心 Service 行覆盖率 82%", COLOR_GREEN),
        ("14 → 0", "已知缺陷 100% 清零", "高危 4 + 中危 7 + 低危 3\n周五功能冻结保障回归闭环", COLOR_AMBER),
    ]
    for i, (big_num, ktitle, kdesc, kcolor) in enumerate(kpis):
        left = 0.45 + i * 2.52
        card = add_card(s2, left, 1.25, 2.36, 2.15, header_color=kcolor)
        tb = s2.shapes.add_textbox(Inches(left + 0.12), Inches(1.42), Inches(2.12), Inches(1.85))
        tf = tb.text_frame
        tf.word_wrap = True
        p1 = tf.paragraphs[0]
        p1.text = big_num
        p1.font.size = Pt(28)
        p1.font.bold = True
        p1.font.color.rgb = kcolor
        p1.font.name = "Microsoft YaHei"
        p2 = tf.add_paragraph()
        p2.text = ktitle
        p2.font.size = Pt(13)
        p2.font.bold = True
        p2.font.color.rgb = COLOR_NAVY
        p2.font.name = "Microsoft YaHei"
        p2.space_after = Pt(6)
        p3 = tf.add_paragraph()
        p3.text = kdesc
        p3.font.size = Pt(10.5)
        p3.font.color.rgb = COLOR_MUTED
        p3.font.name = "Microsoft YaHei"

    add_card(s2, 0.45, 3.65, 6.05, 3.35,
             title="Sprint 6（第 6 周：10.26～11.01）—— 交付 v0.1 主干版",
             bullets=[
                 "冲刺目标：跑通「注册登录 → 发布书籍 → 首页列表 → 详情页 → 站内留言」主干交易闭环，实现 Docker 一键部署。",
                 "完成情况：计划 110 点，实际完成 84 点（完成率 76.4%），提交 30 次代码，关闭 15 个 Issue，发现 9 个 Bug（修复 7 个）。",
                 "关键复盘：前两天前端因等后端建表 SQL 和接口规范干等两天，暴露“任务前置依赖未识别”的假并行问题。"
             ], header_color=COLOR_AMBER, font_size=11.5)

    add_card(s2, 6.80, 3.65, 6.08, 3.35,
             title="Sprint 7（第 7 周：11.02～11.08）—— 交付 v0.2 正式版",
             bullets=[
                 "冲刺目标：清空上周遗留 26 点与历史 Bug，补齐「收藏夹 → 未读提醒 → 编辑回填 → 下架恢复 → Token 黑名单 → 移动端适配」。",
                 "完成情况：计划 64 点（遗留 26 + 新增 38），实际完成 64 点（完成率 100%），提交 28 次代码，关闭 17 个 Issue，清零全部 14 个 Bug。",
                 "关键升级：严格执行“周一 11:30 接口契约先行”与“周五 22:00 功能冻结”，配齐 reset-demo.sh 一键重置脚本。"
             ], header_color=COLOR_GREEN, font_size=11.5)

    # --------------------------------------------------------------------------
    # Slide 3: 双周燃尽图对比（嵌入高清图 01）
    # --------------------------------------------------------------------------
    s3 = prs.slides.add_slide(blank_layout)
    set_slide_background(s3)
    add_header_banner(s3, "二、Scrum 冲刺进程：第 6 周 vs 第 7 周燃尽图演进",
                      "从第 6 周的“前平后陡（76.4%）”到第 7 周的“匀速贴线（100%）”，验证接口契约先行的直接效果", 3)

    s3.shapes.add_picture(chart_paths["01"], Inches(0.45), Inches(1.12), width=Inches(12.43))

    add_card(s3, 0.45, 6.18, 12.43, 1.05,
             bullets=[
                 "核心改进对比：第 6 周前两天仅烧掉 4 点和 11 点（前端干等接口）；第 7 周周一上午 11:30 前张佳鹏与杨昀博先将 4 个新接口契约写入 docs/api.md，前端当天下午直接绑 Mock 联调，周一、周二即烧掉 6 点和 11 点，周五晚准时进入回归收敛期。"
             ], header_color=COLOR_BLUE, font_size=11)

    # --------------------------------------------------------------------------
    # Slide 4: 团队五人分工、工作量与签入统计（嵌入高清图 02）
    # --------------------------------------------------------------------------
    s4 = prs.slides.add_slide(blank_layout)
    set_slide_background(s4)
    add_header_banner(s4, "三、团队五人工作汇报与代码签入汇总（58 次提交）",
                      "每条 Commit 均在 GitHub 仓库中精确绑定对应 Issue（#1 ～ #32），全员协作无死角", 4)

    s4.shapes.add_picture(chart_paths["02"], Inches(0.45), Inches(1.10), width=Inches(12.43))

    member_Editable = [
        ("李科霖 (11次/25点)", "前端骨架 #1、登录注册 #4、路由守卫 #17、个人中心 #9、未读角标轮询与彩排 #24"),
        ("李卓轩 (11次/33点)", "首页列表 #5、详情页 #10、发布与编辑回填 #14、多维筛选 #8、移动适配 #21、收藏页 #25"),
        ("张佳鹏 (13次/32点)", "5张表设计 #2、Result异常兜底 #3、JWT鉴权 #6/#7、收藏接口 #26、未读统计 #27、单测 #28、黑名单 #29"),
        ("杨昀博 (10次/32点)", "书籍CRUD与mustOwn #11、图片上传 #12、留言规则 #15、分页查询 #13、联合索引 #22、下架恢复 #30"),
        ("陈意 (13次/26点)", "Docker一键部署 #18、README #23、Apifox 46条用例 #19、14个Bug跟踪 #20、重置脚本 #31、测试报告 #32")
    ]
    for i, (m_head, m_desc) in enumerate(member_Editable):
        add_card(s4, 0.45 + i * 2.52, 5.92, 2.36, 1.32,
                 title=m_head, bullets=[m_desc], header_color=COLOR_BLUE, font_size=9.5)

    # --------------------------------------------------------------------------
    # Slide 5: 核心架构设计与工程技术亮点
    # --------------------------------------------------------------------------
    s5 = prs.slides.add_slide(blank_layout)
    set_slide_background(s5)
    add_header_banner(s5, "四、系统架构设计与四项关键工程技术突破",
                      "针对校园二手书场景与 2C2G 学生机硬件约束，做出高内聚、低开销的工程方案", 5)

    arch_cards = [
        ("1. 前后端鉴权闭环与 401 防死循环（#6 / #17 / #29）", [
            "密码安全：BCryptPasswordEncoder 加密存储，10 分钟内连续失败 ≥5 次返回 429 锁定。",
            "拦截器分离：后端 JwtInterceptor 只解析不强拦公开 GET 接口；前端 401 拦截器只清 Token，路由跳转统一交由路由守卫判断，彻底根治重定向死循环（BUG-01）。",
            "零外部开销 JWT 黑名单：针对 2C2G 内存限制，不盲目起 Redis，改用 ConcurrentHashMap<String, Long> 存剩余有效期 + @Scheduled 每小时清扫过期项，常数级内存实现注销即刻作废。"
        ], COLOR_BLUE),
        ("2. 数据库设计与联合索引 17 倍提速（#2 / #13 / #22）", [
            "五张核心表：user、book（@TableLogic 逻辑删除）、message（冗余 receiver_id 提速收件箱）、favorite（uk_user_book 唯一约束防并发重）、login_attempt。",
            "多条件安全拼接：BookQuery 价格区间改用 BigDecimal + .ge()/.le() 判非空组装（修复 BUG-04）。",
            "最左前缀联合索引：灌入 5,000 条数据用 EXPLAIN 分析，将单列索引合并为 idx_del_status_cat_created (deleted, status, category, created_at DESC)，消除 Using filesort，查询从 68ms 降至 4ms。"
        ], COLOR_GREEN),
        ("3. 跨模块状态机联动与防越权兜底（#11 / #15 / #30）", [
            "归属权强校验：所有写操作统一走 BookService#mustOwn() 校验书主身份，杜绝越权修改/删除他人书籍，并由 12 条 JUnit 5 + Mockito 单元测试护航。",
            "留言业务边界：MessageService#create() 严格拦截“给自己的书留言（400）”与“向已下架书籍新增留言（409）”，同时保留历史留言可查。",
            "逻辑删除空指针兜底：卖家删书后，买家收藏夹自动降级展示“【该书籍已被卖家删除】”半透明灰标并支持移除，消除跨模块 500 崩溃（BUG-13）。"
        ], COLOR_AMBER),
        ("4. 容器化一键部署与 2C2G 资源精算（#12 / #18 / #31）", [
            "Docker Compose 编排：mysql + backend（Maven 两阶段构建）+ frontend（Nginx 反代 /api 与 /files 静态卷 + try_files 兜底 history 路由）。",
            "内存防 OOM 调优：宿主机挂载 2G swap、JVM 写死 -Xmx512m、MySQL buffer pool 降至 128M，整机内存稳定控制在 62%（解决 BUG-09）。",
            "可重复演示环境：编写 deploy/reset-demo.sh，5 分钟内一键重置 3 个角色、12 本教材与封面文件（docker cp 同步），保障现场演示零翻车。"
        ], COLOR_BLUE)
    ]
    for idx, (atitle, abullets, acolor) in enumerate(arch_cards):
        r = idx // 2
        c = idx % 2
        add_card(s5, 0.45 + c * 6.32, 1.20 + r * 3.02, 6.10, 2.85,
                 title=atitle, bullets=abullets, header_color=acolor, font_size=11)

    # --------------------------------------------------------------------------
    # Slide 6: 测试报告（一）—— 测试体系与性能压测（嵌入高清图 03）
    # --------------------------------------------------------------------------
    s6 = prs.slides.add_slide(blank_layout)
    set_slide_background(s6)
    add_header_banner(s6, "五、Alpha 阶段测试报告（一）：多维测试体系与压测对比",
                      "Apifox 46 条接口用例 100% 通过 + JUnit 12 条核心单测（覆盖率 82%）+ 5,000 条数据慢查询压测", 6)

    s6.shapes.add_picture(chart_paths["03"], Inches(0.45), Inches(1.15), width=Inches(12.43))

    add_card(s6, 0.45, 5.95, 12.43, 1.28,
             title="测试左移与边界覆盖亮点（测试负责人：陈意，配合后端张佳鹏 / 杨昀博）",
             bullets=[
                 "写用例比跑用例更有价值：BUG-01（401 死循环）、BUG-06（手机号空格重复注册）、BUG-10（连点收藏唯一索引冲突 500）均是在构造 Apifox 边界用例时提前撞出来的。",
                 "四大高难边界全覆盖：① 非书主 PUT/DELETE 越权修改拦截（403）；② 连续登录失败 5 次锁定（429）；③ 并发重复收藏捕获 DuplicateKeyException 幂等返回；④ 已下架强行留言拦截（409）。"
             ], header_color=COLOR_GREEN, font_size=11)

    # --------------------------------------------------------------------------
    # Slide 7: 测试报告（二）—— 14 个缺陷跟踪总表（嵌入高清图 04）
    # --------------------------------------------------------------------------
    s7 = prs.slides.add_slide(blank_layout)
    set_slide_background(s7)
    add_header_banner(s7, "六、Alpha 阶段测试报告（二）：14 个缺陷（Bug List）全闭环",
                      "坚持“缺陷进表，不进脑子”（docs/test/buglist.md），高危 4 个、中危 7 个、低危 3 个全部清零", 7)

    s7.shapes.add_picture(chart_paths["04"], Inches(0.45), Inches(1.05), width=Inches(12.43))

    # --------------------------------------------------------------------------
    # Slide 8: 测试报告（三）—— 三大典型用户场景测试走查
    # --------------------------------------------------------------------------
    s8 = prs.slides.add_slide(blank_layout)
    set_slide_background(s8)
    add_header_banner(s8, "七、Alpha 阶段测试报告（三）：三大典型用户场景端到端测试",
                      "基于 reset-demo.sh 内置的 3 个演示角色与 12 本高清教材，完整验证业务主干与异常状态边界", 8)

    scenarios = [
        ("场景测试 1：大二买家“林同学”\n精准淘书、多维筛选、收藏比价与询价", [
            "用户痛点：买全新专业课教材太贵，希望在 15～35 元内淘到 8 成新以上教材，并先收藏比价再约同院系学长自提。",
            "测试链路：",
            "1) 免登录浏览首页响应式卡片流，翻页自动平滑回顶（BUG-02 已修）；",
            "2) 输入关键词“计算机网络”+ 分类“计算机”+ 价格 15~35 元 + 按成色降序筛选（4ms 极速响应，BUG-04 已修）；",
            "3) 点进两本教材详情页，点击星标加入「我的收藏」横向对比价格与成色；",
            "4) 点击底部留言触发路由守卫跳 /login?redirect=/books/3，登录后自动跳回原书详情页并发送自提询价留言。",
            "结论：✅ 100% 通过"
        ], COLOR_BLUE),
        ("场景测试 2：大四卖家“周学长”\n闲置发布、编辑回填、未读红点与安全注销", [
            "用户痛点：毕业前快速拍照挂出二十多本旧书；填错价格能随时回填修改；有人留言能秒看到红点提醒；公共机房退出后防盗号。",
            "测试链路：",
            "1) 登录进 /publish 上传 1.8MB 实拍封面图，发布《CSAPP》但手滑将 45 元填成 450 元；",
            "2) 在个人中心点「编辑」跳 /publish?id=12 自动回填原数据，改回 45 元保存；再点顶部「发布书籍」自动清空旧表单无残留（BUG-11 已修）；",
            "3) 顶部导航栏 el-badge 轮询亮起红点“1”，切到「我收到的留言」Tab 自动调 PUT /read-all 清零红点；",
            "4) 点击退出登录触发 POST /logout 写入服务端黑名单，清理定时器。",
            "结论：✅ 100% 通过"
        ], COLOR_GREEN),
        ("场景测试 3：跨端状态边界联动\n下架原因、409 留言拦截、恢复与删书降级", [
            "用户痛点：线下约好面交需暂留书籍防他人再问；若爽约可一键恢复；若删书绝不能让已收藏买家的页面报 500 崩溃。",
            "测试链路：",
            "1) 卖家周学长在个人中心点「下架」，弹窗输入原因“已约今晚8点图书馆面交”；",
            "2) 买家陈同学点进该书详情页，看到历史留言保留，但发新留言被 409 Conflict 拦截（“已下架不可新增留言”）；",
            "3) 卖家点击「重新上架」可即刻恢复在售；",
            "4) 卖家点击「删除」（@TableLogic 逻辑删除）后，买家打开「我的收藏」无 500 空指针（BUG-13 已修），卡片显示“【该书籍已被卖家删除】”灰标并可一键移除。",
            "结论：✅ 100% 通过（评审会现场一次过）"
        ], COLOR_AMBER)
    ]
    for idx, (stitle, sbullets, scolor) in enumerate(scenarios):
        add_card(s8, 0.45 + idx * 4.22, 1.20, 4.00, 5.85,
                 title=stitle, bullets=sbullets, header_color=scolor, font_size=10.8)

    # --------------------------------------------------------------------------
    # Slide 9: 测试报告（四）—— 四端兼容性矩阵与版本出口条件（嵌入高清图 05）
    # --------------------------------------------------------------------------
    s9 = prs.slides.add_slide(blank_layout)
    set_slide_background(s9)
    add_header_banner(s9, "八、Alpha 阶段测试报告（四）：四端兼容性矩阵与版本出口条件",
                      "真机扫码实测不可替代；结合木桶短板效应动态收敛并 100% 达成四条可量化出口条件", 9)

    s9.shapes.add_picture(chart_paths["05"], Inches(0.45), Inches(1.08), width=Inches(12.43))

    # --------------------------------------------------------------------------
    # Slide 10: 习得的软工原理 / 方法 / 技能（上）
    # --------------------------------------------------------------------------
    s10 = prs.slides.add_slide(blank_layout)
    set_slide_background(s10)
    add_header_banner(s10, "九、习得的软工原理 / 方法 / 技能（上）：敏捷估算与并行工程",
                      "把课本上的敏捷开发、燃尽图诊断与契约驱动开发转化为团队肌肉记忆", 10)

    se_top = [
        ("1. 用户故事拆解 + 故事点（Story Point）相对估算", [
            "踩坑教训：第 6 周对 #8 搜索筛选拍脑袋估 7 点，实际要做前后端四条件组合、页码重置与空参清洗；写测试用例也比跑用例耗时得多。",
            "软工方法：将大功能拆成“填表 → 传图 → 提交 → 跳详情”等小颗粒度子任务分别估点（1 点 ≈ 1 小时净编码/测试）；告别“大概两天做完”这种第一天结束时根本无法度量的估算方式。"
        ], COLOR_BLUE),
        ("2. 燃尽图“看形状，不看数值”与瓶颈识别", [
            "踩坑教训：第 6 周组长李科霖把 #1 骨架和 #4 登录全领了，结果自己埋头写两天，导致前端 B 和后端干等，燃尽图前两天几乎水平。",
            "软工方法：燃尽图的形状是团队协作的心电图。“前平后陡”说明问题不在大家不努力，而在前置依赖未识别。组长的第一要务不是“我先写完”，而是“先消除阻塞，让别人不用等”。"
        ], COLOR_GREEN),
        ("3. 接口契约先行（Contract-First）消除“假并行”", [
            "踩坑教训：没有接口契约的“前后端分开做、最后联调”是假并行，前端静态页没有真数据结构根本无法验收，等待成本以天计。",
            "软工方法：第 7 周严格执行“周一 11:30 前合入 docs/api.md 接口契约文档”（含请求参数、JSON 示例、错误码与 4 条联调注意事项），前端当天下午绑 Mock 数据直接把交互写完，等待期降为 0。"
        ], COLOR_AMBER)
    ]
    for idx, (stitle, sbullets, scolor) in enumerate(se_top):
        add_card(s10, 0.45, 1.22 + idx * 1.98, 12.43, 1.80,
                 title=stitle, bullets=sbullets, header_color=scolor, font_size=11.5)

    # --------------------------------------------------------------------------
    # Slide 11: 习得的软工原理 / 方法 / 技能（下）
    # --------------------------------------------------------------------------
    s11 = prs.slides.add_slide(blank_layout)
    set_slide_background(s11)
    add_header_banner(s11, "十、习得的软工原理 / 方法 / 技能（下）：质量护城河与工程纪律",
                      "出了问题先解决、再复盘、不甩锅，把每一次踩坑都变成 README 里的硬规矩", 11)

    se_bot = [
        ("4. 聚焦阻塞的站会机制 + 周五晚功能冻结（Feature Freeze）", [
            "站会只问“有没有卡住”：第 6 周周四 20 分钟线上站会果断把“先抠样式后联调”调转过来，后两天产出直接翻倍。",
            "功能冻结是质量的护城河：第 7 周五 22:00 准时停止合入新功能，留出完整周末做全量回归，成功抓出 BUG-13（删书后买家收藏夹空指针）这种单模块自测极易漏掉的跨模块缺陷。"
        ], COLOR_BLUE),
        ("5. 配置管理纪律：Issue 先行、Commit 溯源与 PR 准入保护", [
            "惊魂事故复盘：第 6 周张佳鹏为回滚错误合并直接 git reset --hard 强推 main，冲掉了杨昀博刚推的两个提交（靠本地分支恢复）。",
            "工程铁律落地：当晚开启 main 分支保护，立下四条规矩——① 无 Issue 不编码；② 58 次 Commit 100% 带 Issue 号；③ 严禁强推，所有代码必须走 PR 经另一人 Review 合并，回滚一律 git revert；④ 12 条 JUnit 单测给 mustOwn() 重构兜底。"
        ], COLOR_GREEN),
        ("6. 资源受限下的架构权衡（Trade-off）与可重复演示交付物", [
            "告别“在我电脑上能跑”：每周一次部署让我们提前撞上容器路径 404、Nginx 413、2C2G OOM 三大环境坑；在 2C2G 约束下放弃重型 Redis，用内存 Map + 定时清扫实现零外部开销的 Token 黑名单。",
            "可重复演示环境也是工程交付物：把演示数据与 12 张封面做成 reset-demo.sh 一键重置脚本，彻底告别现场点到脏数据的窘境。"
        ], COLOR_AMBER)
    ]
    for idx, (stitle, sbullets, scolor) in enumerate(se_bot):
        add_card(s11, 0.45, 1.22 + idx * 1.98, 12.43, 1.80,
                 title=stitle, bullets=sbullets, header_color=scolor, font_size=11.5)

    # --------------------------------------------------------------------------
    # Slide 12: 第 8 周收尾与 Beta 阶段规划展望
    # --------------------------------------------------------------------------
    s12 = prs.slides.add_slide(blank_layout)
    set_slide_background(s12)
    add_header_banner(s12, "十一、Alpha 阶段总结与 Beta 阶段迭代规划",
                      "两周 14 天、58 次提交、32 个 Issue、46 条接口用例，跑通属于我们五人自己的 Scrum 节奏", 12)

    beta_plan = [
        ("李科霖（组长 / 前端 A）", [
            "第 8 周收尾：发布《习得的软工原理/方法/技能》与团队目录博客，组织 Alpha 答辩汇报。",
            "Beta 核心规划：引入 WebSocket 替代 30 秒定时轮询，实现买家与卖家站内信实时对话流。"
        ]),
        ("李卓轩（前端 B）", [
            "第 8 周收尾：整理前端组件规范文档，录制 3 分钟系统端到端演示视频。",
            "Beta 核心规划：支持书籍多图上传轮播展示、首页加载骨架屏（Skeleton）与暗黑模式。"
        ]),
        ("张佳鹏（后端 A）", [
            "第 8 周收尾：整理数据库 ER 图与接口安全章节，配置 GitHub Actions 自动跑 JUnit 单测。",
            "Beta 核心规划：接入 ISBN 扫码 / 输入自动回填书名、作者、出版社与标准封面图。"
        ]),
        ("杨昀博（后端 B）", [
            "第 8 周收尾：整理 5,000 条数据慢查询优化 EXPLAIN 对比报告，维护线上试用环境稳定。",
            "Beta 核心规划：支持按“学院 / 课程标签”精准筛选教材，新增买家“求购专区”模块。"
        ]),
        ("陈意（测试 / 部署 / 文档）", [
            "第 8 周收尾：正式发布《Alpha 阶段测试报告》博客，发放并收集外组同学扫码试用反馈问卷。",
            "Beta 核心规划：编写 Beta 阶段自动化回归测试流水线与多并发压力测试报告。"
        ]),
        ("Alpha 阶段答辩致谢", [
            "代码仓库：github.com/shiguang-team/shiguang-book",
            "任务看板：github.com/shiguang-team/shiguang-book/projects/1",
            "感谢老师与助教的审阅指导！欢迎现场扫码体验 v0.2！"
        ])
    ]
    for idx, (btitle, bbullets) in enumerate(beta_plan):
        r = idx // 3
        c = idx % 3
        hcolor = COLOR_GREEN if idx == 5 else COLOR_BLUE
        add_card(s12, 0.45 + c * 4.22, 1.25 + r * 2.95, 4.00, 2.70,
                 title=btitle, bullets=bbullets, header_color=hcolor, font_size=11)

    prs.save(PPTX_FILENAME)
    print(f"\n[Success] PPT 已成功生成: { os.path.abspath(PPTX_FILENAME) }")


# ==============================================================================
# 主程序入口
# ==============================================================================
if __name__ == "__main__":
    print("=== 开始生成拾光小组 Alpha 阶段图表 (PNG + SVG) ===")
    chart_files = {
        "01": make_chart_01_burndown(),
        "02": make_chart_02_workload(),
        "03": make_chart_03_testing_performance(),
        "04": make_chart_04_buglist_table(),
        "05": make_chart_05_compatibility_matrix(),
    }
    print("\n=== 开始组装 16:9 宽屏答辩 PPT (.pptx) ===")
    build_presentation(chart_files)
    print("=== 全部任务完成！===")