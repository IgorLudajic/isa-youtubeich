"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";

export interface WatchPartyDto {
  id: number;
  name: string;
  ownerUsername: string;
  isOwner: boolean;
}

const baseUrl = process.env.API_BASE_URL!;

export async function getWatchParties(): Promise<WatchPartyDto[]> {
  const res = await fetch(`${baseUrl}/api/watch-party`, { cache: "no-store" });
  if (!res.ok) throw new Error("Failed to fetch watch parties");
  return res.json();
}

export async function getWatchPartyDetails(id: string): Promise<WatchPartyDto> {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  const headers: Record<string, string> = {};
  
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(`${baseUrl}/api/watch-party/${id}`, { 
    headers,
    cache: "no-store" 
  });
  
  if (!res.ok) throw new Error("Failed to fetch party details");
  return res.json();
}

export async function createWatchParty(name: string): Promise<WatchPartyDto> {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;

  if (!token) {
    redirect("/login");
  }

  const res = await fetch(`${baseUrl}/api/watch-party`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`,
    },
    body: JSON.stringify({ name }),
  });

  if (!res.ok) throw new Error("Failed to create party");
  return res.json();
}

export async function playVideoInParty(partyId: string, videoId: number): Promise<void> {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;

  if (!token) {
    redirect("/login");
  }

  const res = await fetch(`${baseUrl}/api/watch-party/${partyId}/play/${videoId}`, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${token}`,
    },
  });

  if (!res.ok) throw new Error("Failed to start video");
}