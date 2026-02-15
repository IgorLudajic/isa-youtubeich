"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getWatchPartyDetails, playVideoInParty } from "@/lib/watchparty";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { PlayFilledAlt, UserSpeaker } from "@carbon/icons-react";
import PartyVideoPicker from "@/components/PartyVideoPicker";

export default function PartyPage() {
  const { id } = useParams();
  const router = useRouter();
  const [party, setParty] = useState<any>(null);
  const [status, setStatus] = useState("Connecting...");
  const stompClient = useRef<Client | null>(null);

  useEffect(() => {
    getWatchPartyDetails(id as string).then(setParty).catch(() => {
      setStatus("Failed to load party.");
    });
  }, [id]);

  useEffect(() => {
    if (!id) return;

    const socket = new SockJS(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080"}/ws`);
    
    const client = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        setStatus("Connected. Waiting for host...");
        client.subscribe(`/topic/party/${id}`, (msg) => {
          const event = JSON.parse(msg.body);
          if (event.type === "REDIRECT") {
            console.log("Redirecting to video:", event.videoId);
            router.push(`/video/${event.videoId}`);
          }
        });
      },
      onDisconnect: () => setStatus("Disconnected"),
    });

    client.activate();
    stompClient.current = client;

    return () => {
      client.deactivate();
    };
  }, [id, router]);

  const handlePlay = async (videoId: number) => {
    try {
      await playVideoInParty(id as string, videoId);
    } catch (e) {
      alert("Failed to start video. Check ID.");
    }
  };

  if (!party) return <div className="p-10 text-center">{status}</div>;

  return (
    <div className="max-w-2xl mx-auto mt-10">
      <Card className="border-purple-900 bg-purple-50">
        <CardHeader className="bg-purple-200 border-b-2 border-border">
          <CardTitle className="flex items-center gap-2">
            <UserSpeaker className="w-6 h-6" />
            Watch Party: {party.name}
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-6 pt-6 text-center">
          
          <div className="bg-white p-4 rounded-base border-2 border-purple-200">
             <p className="text-sm font-mono text-gray-500 mb-1">Status</p>
             <p className="font-bold text-purple-700 animate-pulse">{status}</p>
          </div>

          <div className="space-y-2">
            <p>Domaćin sobe je <span className="font-bold">@{party.ownerUsername}</span>.</p>
            {!party.isOwner && (
              <p className="text-sm text-gray-600">
                Molimo sačekajte. Kada kreator sobey pusti video, vaš pretraživač će automatski otvoriti taj video.
              </p>
            )}
          </div>

          {party.isOwner ? (
            <PartyVideoPicker onPlayVideo={handlePlay} />
          ) : (
            <div className="p-8 bg-purple-100 rounded-base border-2 border-purple-200 border-dashed">
              <p className="animate-pulse">Kreator sobe bira sledeći video...</p>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}