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

export interface CommentDto {
  id: number;
  text: string;
  username: string;
  createdAt: string;
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

export async function likeVideo(id: number, redirectUrl?: string) {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  if (!token)
    redirect(`/login?redir=${encodeURIComponent(redirectUrl || "/")}`);
  const res = await fetch(`${baseUrl}/api/reactions/video/${id}?type=LIKE`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error("Failed to like video");
}

export async function dislikeVideo(id: number, redirectUrl?: string) {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  if (!token)
    redirect(`/login?redir=${encodeURIComponent(redirectUrl || "/")}`);
  const res = await fetch(`${baseUrl}/api/reactions/video/${id}?type=DISLIKE`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error("Failed to dislike video");
}

export async function getVideoComments(
  videoId: number,
  page: number = 0,
  size: number = 10,
): Promise<Page<CommentDto>> {
  const res = await fetch(
    `${baseUrl}/api/comments/video/${videoId}?page=${page}&size=${size}`,
  );
  if (!res.ok) throw new Error("Failed to fetch comments");
  return res.json();
}

export async function postComment(
  videoId: number,
  text: string,
  redirectUrl?: string,
): Promise<CommentDto> {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  if (!token)
    redirect(`/login?redir=${encodeURIComponent(redirectUrl || "/")}`);
  const res = await fetch(`${baseUrl}/api/comments/video/${videoId}`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ text }),
  });
  if (!res.ok) throw new Error("Failed to post comment");
  return res.json();
}

export async function uploadVideo(formData: FormData): Promise<void> {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;

  if (!token) {
    redirect("/login");
  }

  const res = await fetch(`${baseUrl}/api/videos`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: formData,
  });

  if (!res.ok) {
    const errorText = await res.text();
    throw new Error(errorText || "Video upload failed");
  }
}

export async function viewVideo(id: number): Promise<void> {
  await fetch(`${baseUrl}/api/videos/${id}/view`, {
    method: "POST",
  });
}
