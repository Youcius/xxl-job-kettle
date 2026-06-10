# Issue tracker：GitHub

本仓库的 Issues 和 PRD 记录在 GitHub Issues 中。所有 issue 相关操作默认使用 `gh` CLI。

仓库地址：

```text
https://github.com/Youcius/xxl-job-kettle
```

## 约定

- **创建 issue**：使用 `gh issue create --title "..." --body "..."`
  - 多行正文使用 heredoc。
- **读取 issue**：使用 `gh issue view <number> --comments`
  - 需要时同时读取 labels 和 comments。
- **列出 issue**：
  ```bash
  gh issue list --state open --json number,title,body,labels,comments --jq '[.[] | {number, title, body, labels: [.labels[].name], comments: [.comments[].body]}]'
  ```
  - 可按需增加 `--label`、`--state` 等过滤参数。
- **评论 issue**：使用 `gh issue comment <number> --body "..."`
- **添加/移除标签**：
  ```bash
  gh issue edit <number> --add-label "..."
  gh issue edit <number> --remove-label "..."
  ```
- **关闭 issue**：
  ```bash
  gh issue close <number> --comment "..."
  ```

在仓库目录内运行 `gh` 时，通常可以自动从 `git remote -v` 推断仓库。

## 当某个 skill 说“publish to the issue tracker”

在 `Youcius/xxl-job-kettle` 的 GitHub Issues 中创建 issue。

## 当某个 skill 说“fetch the relevant ticket”

运行：

```bash
gh issue view <number> --comments
```
