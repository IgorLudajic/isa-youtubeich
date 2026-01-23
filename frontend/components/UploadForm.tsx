"use client";

import { useState } from "react";
import dynamic from "next/dynamic";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { uploadVideo } from "@/lib/videofeed";
import { Calendar, Location, Tag } from "@carbon/icons-react";

const MapPicker = dynamic(() => import("@/components/MapPicker"), {
  ssr: false,
  loading: () => (
    <div className="h-75 w-full bg-gray-100 rounded-xl flex items-center justify-center text-gray-500 animate-pulse">
      Učitavanje mape...
    </div>
  ),
});

export default function UploadForm() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [location, setLocation] = useState<{ lat: number; lng: number } | null>(
    null,
  );
  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);
    setError(null);

    const form = event.currentTarget;
    const formData = new FormData(form);

    if (location) {
      formData.append("latitude", location.lat.toString());
      formData.append("longitude", location.lng.toString());
    }

    const videoFile = formData.get("videoFile") as File;
    if (videoFile && videoFile.size > 200 * 1024 * 1024) {
      setError("Video fajl je preveliki (maksimalno 200MB).");
      setIsSubmitting(false);
      return;
    }

    try {
      await uploadVideo(formData);
      alert(
        "Video je uspešno postavljen! \nBićete preusmereni na početnu stranu.",
      );
      window.location.href = "/";
    } catch (e: any) {
      console.error(e);
      setError(e.message || "Došlo je do greške prilikom postavljanja videa.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Card className="w-full max-w-2xl mx-auto my-8 shadow-lg">
      <CardHeader>
        <CardTitle className="text-2xl flex items-center gap-2">
          Postavi novi video
        </CardTitle>
        <CardDescription>
          Podelite svoj sadržaj sa svetom. Popunite detalje ispod.
        </CardDescription>
      </CardHeader>

      <form onSubmit={handleSubmit}>
        <CardContent className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="title" className="font-bold">
              Naslov videa
            </Label>
            <Input
              id="title"
              name="title"
              placeholder="Unesite naslov..."
              required
              disabled={isSubmitting}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="description" className="font-bold">
              Opis
            </Label>
            <Textarea
              id="description"
              name="description"
              placeholder="O čemu se radi u videu?"
              required
              disabled={isSubmitting}
              className="min-h-25"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label
                htmlFor="tags"
                className="flex items-center gap-2 font-bold"
              >
                <Tag /> Tagovi
              </Label>
              <Input
                id="tags"
                name="tags"
                placeholder="npr. muzika, vlog, putovanje"
                disabled={isSubmitting}
              />
              <p className="text-xs text-gray-500">Odvojite tagove zarezom.</p>
            </div>

            <div className="space-y-2">
              <Label
                htmlFor="premieresAt"
                className="flex items-center gap-2 font-bold"
              >
                <Calendar /> Premijera (Opciono)
              </Label>
              <Input
                id="premieresAt"
                name="premieresAt"
                type="datetime-local"
                disabled={isSubmitting}
              />
              <p className="text-xs text-gray-500">
                Video će biti skriven do ovog datuma.
              </p>
            </div>
          </div>

          <div className="space-y-2">
            <Label className="flex items-center gap-2 font-bold">
              <Location /> Lokacija snimanja (Opciono)
            </Label>

            <div className="border-2 border-gray-200 rounded-xl overflow-hidden shadow-sm">
              <MapPicker
                position={location}
                onLocationSelect={(lat, lng) => setLocation({ lat, lng })}
              />
            </div>

            {location ? (
              <div className="flex items-center justify-between text-sm bg-green-50 text-green-700 p-2 rounded-md border border-green-200">
                <span>
                  {" "}
                  Odabrano: {location.lat.toFixed(5)}, {location.lng.toFixed(5)}
                </span>
                <Button
                  type="button"
                  variant="neutral"
                  size="sm"
                  className="text-red-600 hover:text-red-700 hover:bg-red-50 h-auto py-0"
                  onClick={() => setLocation(null)}
                >
                  Ukloni
                </Button>
              </div>
            ) : (
              <p className="text-xs text-gray-500">
                Kliknite na mapu da označite lokaciju.
              </p>
            )}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-2">
            <div className="space-y-2">
              <Label htmlFor="videoFile" className="font-bold">
                Video fajl (MP4)
              </Label>
              <Input
                id="videoFile"
                name="videoFile"
                type="file"
                accept="video/mp4,video/webm"
                required
                disabled={isSubmitting}
                className="cursor-pointer file:cursor-pointer file:font-semibold file:text-foreground"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="thumbnailFile" className="font-bold">
                Sličica (Thumbnail)
              </Label>
              <Input
                id="thumbnailFile"
                name="thumbnailFile"
                type="file"
                accept="image/png,image/jpeg,image/webp"
                required
                disabled={isSubmitting}
                className="cursor-pointer file:cursor-pointer file:font-semibold"
              />
            </div>
          </div>

          {error && (
            <div className="p-4 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-md">
              <p className="font-bold">Greška</p>
              <p>{error}</p>
            </div>
          )}
        </CardContent>
        <CardFooter className="flex justify-end pt-2 pb-6 px-6">
          <Button
            type="submit"
            size="lg"
            disabled={isSubmitting}
            className="w-full md:w-auto font-bold"
          >
            {isSubmitting ? "Postavljanje..." : "Objavi Video"}
          </Button>
        </CardFooter>
      </form>
    </Card>
  );
}
