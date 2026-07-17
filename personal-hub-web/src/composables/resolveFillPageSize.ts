/** 按主内容区可视高度决定一屏条数（PAGE_SPEC 矮屏降档） */
export function resolveFillPageSize(contentHeightPx: number): number {
  if (contentHeightPx >= 640) return 10
  if (contentHeightPx >= 520) return 8
  return 6
}
