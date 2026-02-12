"use client";

import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Send } from "@carbon/icons-react";

export default function VideoChat({ videoId, username }: { videoId: number; username: string }) {
  const [messages, setMessages] = useState<{sender: string, content: string}[]>([]);
  const [input, setInput] = useState("");
  const [connected, setConnected] = useState(false);
  const stompClient = useRef<Client | null>(null);

  useEffect(() => {
    if (stompClient.current && stompClient.current.active) return;

    const socket = new SockJS(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080"}/ws`);

    stompClient.current = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        setConnected(true);
        stompClient.current?.subscribe(`/topic/video/${videoId}`, (msg) => {
          setMessages(prev => [...prev, JSON.parse(msg.body)]);
        });
      },
      onDisconnect: () => setConnected(false),
    });

    stompClient.current.activate();

    return () => {
      stompClient.current?.deactivate();
    };
  }, [videoId]);

  const send = (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || !connected) return;

    stompClient.current?.publish({
      destination: `/app/chat/${videoId}`,
      body: JSON.stringify({ sender: username, content: input, videoId })
    });
    setInput("");
  };

  return (
    <Card className="h-[500px] flex flex-col border-2 border-border shadow-shadow bg-background p-0 gap-0 overflow-hidden">

      {/* Header - sada dodiruje vrh i ivice */}
      <CardHeader className="py-3 px-4 bg-secondary-background border-b-2 m-0 rounded-none">
        <CardTitle className="text-sm flex items-center justify-between">
          Čet uživo <span className={`h-2 w-2 rounded-full ${connected ? "bg-green-500" : "bg-red-500"}`} />
        </CardTitle>
      </CardHeader>

      <CardContent className="flex-1 overflow-y-auto p-4 space-y-2 font-mono text-sm">
        {messages.map((m, i) => (
          <div key={i} className="break-words leading-tight">
            <span className="font-bold text-lime-700">@{m.sender}:</span> {m.content}
          </div>
        ))}
      </CardContent>

      <form onSubmit={send} className="p-3 border-t-2 flex gap-2 bg-secondary-background m-0">
        <Input
            value={input}
            onChange={e => setInput(e.target.value)}
            placeholder="Poruka..."
            className="h-9 text-sm"
        />
        <Button type="submit" size="sm" disabled={!connected}><Send size={16}/></Button>
      </form>

    </Card>
  );
}