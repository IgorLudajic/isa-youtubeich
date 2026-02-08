"use client";

import { Suspense, useEffect, useState } from "react";

function useHydration() {
  const [hydrated, setHydrated] = useState(false);
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setHydrated(true);
  }, []);
  return hydrated;
}

export default function LocalTime({ date }: { date: number | string | Date }) {
  // return <>{new Date(date).toLocaleTimeString()}</>;
  const hydrated = useHydration();
  return (
    <Suspense key={hydrated ? "local" : "utc"}>
      <time dateTime={new Date(date).toISOString()}>
        {new Date(date).toLocaleTimeString(Intl.Locale.name, {
          timeStyle: "short",
        })}
        {hydrated ? "" : " (UTC)"}
      </time>
    </Suspense>
  );
}
