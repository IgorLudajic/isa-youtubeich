"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getClientToken } from "@/lib/auth"; // Importujemo funkciju za dobavljanje tokena

export default function TestPage() {
  const [res, setRes] = useState("");
  const [loading, setLoading] = useState(false);

  const runTest = async () => {
    setLoading(true);
    setRes("");

    try {
      // 1. Dobavljamo token direktno sa servera (iz HTTP-only kolačića)
      const token = await getClientToken();

      // Debug log za konzolu
      console.log("Token status:", token ? "Pronađen" : "Nije pronađen");

      if (!token) {
        setRes("Greška: Niste ulogovani ili je sesija istekla. Molimo ulogujte se ponovo.");
        setLoading(false);
        return;
      }

      // 2. Slanje zahteva na Backend
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080"}/api/benchmark/run`,
        {
          method: "GET",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      if (response.status === 401) {
        setRes("Greška 401: Niste autorizovani. Backend nije prihvatio token.");
      } else if (response.status === 403) {
        setRes("Greška 403: Nemate ROLE_ADMIN privilegiju za pokretanje testa.");
      } else {
        const data = await response.text();
        setRes(data);
      }
    } catch (err) {
      console.error(err);
      setRes("Greška pri povezivanju sa bekom. Proverite da li su Docker kontejneri pokrenuti.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6 pt-10">
      <Card className="border-2 border-border shadow-shadow bg-main/10">
        <CardHeader>
          <CardTitle className="text-xl">3.14. MQ JSON vs Protobuf Benchmark</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <Button
            onClick={runTest}
            disabled={loading}
            className="w-full bg-lime-500 hover:bg-lime-600 text-black font-bold border-2 border-black"
          >
            {loading ? "Simulacija u toku (50 poruka)..." : "Pokreni Benchmark"}
          </Button>

          {res && (
            <div className="relative">
              <div className="absolute top-2 right-2 text-[10px] text-green-800 font-bold uppercase">Result Log</div>
              <pre className="p-4 bg-black text-green-400 font-mono text-xs rounded-base whitespace-pre-wrap border-2 border-border min-h-[150px]">
                {res}
              </pre>
            </div>
          )}
        </CardContent>
      </Card>

      <div className="text-center text-[10px] text-stone-500 italic px-4">
        *Ovaj test upoređuje brzinu serijalizacije i veličinu paketa podataka između standardnog JSON formata
        i Google Protocol Buffers (Protobuf) formata preko RabbitMQ poruka.
      </div>
    </div>
  );
}