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
import {
  Calendar,
  Close,
  DocumentVideo,
  Location,
  Tag,
  Thumbnail_1,
} from "@carbon/icons-react";

const MapPicker = dynamic(() => import("@/components/MapPicker"), {
  ssr: false,
  loading: () => (
    <div className="max-h-100 aspect-[1.5] w-full bg-gray-100 text-gray-500 animate-pulse rounded-base border-border border-2 flex items-center justify-center">
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
      // Convert premieresAt from local time to UTC if present
      const premieresAt = formData.get("premieresAt") as string;
      if (premieresAt) {
        const localDate = new Date(premieresAt);
        const utcString = localDate.toISOString().slice(0, -1);
        formData.set("premieresAt", utcString);
      }

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
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="text-2xl flex items-center gap-2">
          Postavi novi video
        </CardTitle>
        <CardDescription>
          Podelite svoj sadržaj sa svetom. Popunite detalje ispod.
        </CardDescription>
      </CardHeader>

      <CardContent>
        <form id="uploadForm" onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="title" className="font-bold ml-px">
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
            <Label htmlFor="description" className="font-bold ml-px">
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
                className="flex items-center gap-1 font-bold ml-px"
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
                className="flex items-center gap-1 font-bold ml-px"
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
            <Label className="flex items-center gap-1 font-bold ml-px">
              <Location /> Lokacija snimanja (Opciono)
            </Label>

            <MapPicker
              position={location}
              onLocationSelect={(lat, lng) => setLocation({ lat, lng })}
            />

            {location ? (
              <div className="flex items-center justify-between text-sm text-lime-800 bg-lime-100 p-2 rounded-base border-2 border-border rounded-t-none -mt-3 z-1 relative">
                <div>
                  <span className="font-bold mr-1">Odabrano:</span>
                  <span>
                    {location.lat.toFixed(5)}, {location.lng.toFixed(5)}
                  </span>
                </div>
                <button
                  className="text-red-600 hover:text-red-700 bg-red-50 hover:bg-red-100 border-[1.5px] border-red-600 rounded-full cursor-pointer"
                  onClick={() => setLocation(null)}
                >
                  <Close />
                </button>
              </div>
            ) : (
              <p className="text-xs text-gray-500 ml-px">
                Kliknite na mapu da označite lokaciju.
              </p>
            )}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-2">
            <div className="space-y-2">
              <Label
                htmlFor="videoFile"
                className="flex items-center gap-1 font-bold ml-px"
              >
                <DocumentVideo />
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
              <Label
                htmlFor="thumbnailFile"
                className="flex items-center gap-1 font-bold ml-px"
              >
                <Thumbnail_1 />
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
        </form>
      </CardContent>
      <CardFooter className="flex justify-end">
        <Button
          type="submit"
          form="uploadForm"
          size="lg"
          disabled={isSubmitting}
          className="w-full md:w-auto font-bold"
        >
          Objavi video
        </Button>
      </CardFooter>
    </Card>
  );
}
