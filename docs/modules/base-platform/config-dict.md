# Dictionary And Config

## Current Status

已实现。

## Tables

- `sys_dict_type`
- `sys_dict_item`
- `sys_config`

## Frontend Pages

- `/system/dicts`
- `/system/configs`

## Backend APIs

- `/api/system/dict/types`
- `/api/system/dict/items`
- `/api/system/configs`

## Rules

- 字典编码唯一。
- 字典项编码在同类型下唯一。
- 配置键唯一。
- 禁用项不应作为普通业务可选项返回。
- 写操作记录操作日志。

## Verification

- 可新增、编辑、启用/禁用、删除。
- 重复编码返回明确错误。

