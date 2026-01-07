"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";

export interface VideoHomeDto {
  Id: number;
  title: string;
  thumbnailUrl: string;
  viewCount: number;
  likes: number;
  dislikes: number;
  createdAt: string;
  creatorUsername: string;
}

export interface VideoDetailsDto {
  Id: number;
  title: string;
  thumbnailUrl: string;
  viewCount: number;
  likes: number;
  dislikes: number;
  likedByCurrentUser: boolean;
  dislikedByCurrentUser: boolean;
  createdAt: string;
  creatorUsername: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

const baseUrl = process.env.API_BASE_URL!;

export async function getHomeFeed(
  page: number = 0,
  size: number = 10,
): Promise<Page<VideoHomeDto>> {
  const res = await fetch(`${baseUrl}/api/videos?page=${page}&size=${size}`);
  if (!res.ok) throw new Error("Failed to fetch home feed");
  return res.json();
}

export async function getVideoDetails(id: number): Promise<VideoDetailsDto> {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  const headers: Record<string, string> = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  const res = await fetch(`${baseUrl}/api/videos/${id}`, { headers });
  if (!res.ok) throw new Error("Failed to fetch video details");
  return res.json();
}

export async function likeVideo(id: number) {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  if (!token) redirect("/login");
  const res = await fetch(`${baseUrl}/api/reactions/video/${id}?type=LIKE`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error("Failed to like video");
}

export async function dislikeVideo(id: number) {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  if (!token) redirect("/login");
  const res = await fetch(`${baseUrl}/api/reactions/video/${id}?type=DISLIKE`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error("Failed to dislike video");
}
