import Link from "next/link";
import { UserMultiple } from "@carbon/icons-react";
import { Card } from "@/components/ui/card";

export default function WatchPartyCard({ party }: { party: any }) {
  return (
    <Link href={`/party/${party.id}`}>
      <div className="w-[250px] h-[100px] rounded-base border-2 border-border bg-purple-200 shadow-shadow hover:translate-x-boxShadowX hover:translate-y-boxShadowY hover:shadow-none transition-all flex flex-col justify-center px-4 cursor-pointer relative overflow-hidden">
        <UserMultiple className="absolute -right-2 -bottom-2 w-16 h-16 text-purple-400/50" />
        <h3 className="font-heading text-lg truncate pr-2">{party.name}</h3>
        <p className="text-sm font-mono text-purple-900">Host: @{party.ownerUsername}</p>
        <div className="absolute top-2 right-2 w-2 h-2 bg-green-500 rounded-full animate-pulse" />
      </div>
    </Link>
  );
}