"use client";

import { Button } from "@/components/ui/button";
import { createWatchParty } from "@/lib/watchparty";
import { useRouter } from "next/navigation";

export default function CreatePartyButton() {
  const router = useRouter();

  const handleCreate = async () => {
    const name = prompt("Unesite ime Watch Party sobe:");
    if (name) {
      try {
        const wp = await createWatchParty(name);
        router.refresh();
        router.push(`/party/${wp.id}`);
      } catch (e) {
        alert("Greška pri kreiranju sobe.");
      }
    }
  };

  return (
    <Button onClick={handleCreate} size="sm" className="bg-purple-500 text-white hover:bg-purple-600">
      + Nova Soba
    </Button>
  );
}