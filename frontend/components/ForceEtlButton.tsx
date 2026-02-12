"use client";

import { forceRunPopularityPipeline } from "@/lib/videofeed";
import { Button } from "@/components/ui/button";

export default function ForceEtlButton({}) {
  return (
    <Button onClick={() => forceRunPopularityPipeline()}>Poteraj ETL</Button>
  );
}
