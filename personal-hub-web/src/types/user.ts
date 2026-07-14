/** 性别 */
export type Gender = 0 | 1 | 2

/** 个人资料 */
export interface ProfileVO {
  id: number
  username: string
  nickname: string
  avatar: string | null
  email: string
  gender: Gender
  birthday: string | null
  phone: string | null
  country: string | null
  province: string | null
  city: string | null
  district: string | null
  website: string | null
  github: string | null
  bio: string | null
  createdAt: string
}

/** 更新个人资料 */
export interface ProfileUpdateDTO {
  nickname: string
  email: string
  gender: Gender
  birthday: string | null
  phone: string | null
  country: string | null
  province: string | null
  city: string | null
  district: string | null
  website: string | null
  github: string | null
  bio: string | null
}
