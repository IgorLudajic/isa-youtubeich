export function declOfNum(
  num: number,
  forms: [string, string, string],
): string {
  const n = Math.abs(num) % 100;
  const n1 = n % 10;
  if (n > 10 && n < 20) return forms[2];
  if (n1 > 1 && n1 < 5) return forms[1];
  if (n1 === 1) return forms[0];
  return forms[2];
}

export const i18n = {
  likes: (count: number) =>
    `${declOfNum(count, ["sviđanje", "sviđanja", "sviđanja"])}`,
  dislikes: (count: number) =>
    `${declOfNum(count, ["nesviđanje", "nesviđanja", "nesviđanja"])}`,
  views: (count: number) =>
    `${declOfNum(count, ["pregled", "pregleda", "pregleda"])}`,
  comments: (count: number) =>
    `${declOfNum(count, ["komentar", "komentara", "komentara"])}`,
};
