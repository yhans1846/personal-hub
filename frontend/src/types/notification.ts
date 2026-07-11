export interface NotificationVO {
  id: number
  type: string
  title: string
  content: string | null
  isRead: boolean
  relatedId: number | null
  relatedType: string | null
  createdAt: string
}
