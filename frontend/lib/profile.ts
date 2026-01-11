"use server";

import { Page, VideoHomeDto } from "./videofeed";

const baseUrl = process.env.API_BASE_URL!;

export interface UserPublicProfileDto {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  createdAt: string;
  videos: Page<VideoHomeDto>;
}

export async function getPublicProfile(username: string, page: number = 0, size: number = 10) : Promise<UserPublicProfileDto> {
  const res = await fetch(
    `${baseUrl}/api/users/${username}/profile?page=${page}&size=${size}`
  );
  if(!res.ok) throw new Error("Failed to fetch public profile");
  return res.json();
}