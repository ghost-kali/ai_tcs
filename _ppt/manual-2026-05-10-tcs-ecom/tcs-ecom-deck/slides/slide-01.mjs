export default async function addSlide(presentation, ctx) {
  const slide = presentation.slides.add();

  const colors = {
    ink: "#0F172A",
    muted: "#475569",
    navy: "#0B2E4A",
    teal: "#00A3A3",
    bg: "#FFFFFF",
    card: "#F8FAFC",
  };

  ctx.addShape(slide, { x: 0, y: 0, w: ctx.W, h: ctx.H, fill: colors.bg });
  ctx.addShape(slide, { x: 0, y: 0, w: ctx.W, h: 12, fill: colors.teal });
  ctx.addShape(slide, { x: 0, y: 12, w: ctx.W, h: 74, fill: colors.navy });

  ctx.addText(slide, {
    x: 72,
    y: 30,
    w: ctx.W - 144,
    h: 44,
    text: "E‑commerce Microservices Platform",
    fontSize: 34,
    bold: true,
    color: "#FFFFFF",
    typeface: ctx.fonts.title,
  });

  ctx.addText(slide, {
    x: 72,
    y: 86 + 54,
    w: 720,
    h: 120,
    text: "Spring Boot • Spring Cloud • API Gateway • JWT • Redis • PostgreSQL",
    fontSize: 18,
    color: colors.muted,
  });

  ctx.addShape(slide, {
    x: 72,
    y: 260,
    w: ctx.W - 144,
    h: 320,
    fill: colors.card,
    line: ctx.line("#E2E8F0", 1),
  });

  ctx.addText(slide, {
    x: 96,
    y: 288,
    w: ctx.W - 192,
    h: 46,
    text: "What this deck covers",
    fontSize: 22,
    bold: true,
    color: colors.ink,
  });

  const bullets = [
    "Project overview and business goal",
    "System architecture (frontend → gateway → microservices)",
    "Key features delivered (auth, catalog, cart/order, payment)",
    "Security approach and learnings",
    "Next steps for production hardening",
  ];

  ctx.addText(slide, {
    x: 104,
    y: 344,
    w: ctx.W - 220,
    h: 220,
    text: bullets.map((b) => `• ${b}`).join("\n"),
    fontSize: 18,
    color: colors.ink,
  });

  const today = new Date();
  const dateText = today.toLocaleDateString("en-GB", { year: "numeric", month: "short", day: "2-digit" });

  ctx.addText(slide, {
    x: 72,
    y: ctx.H - 44,
    w: ctx.W - 144,
    h: 28,
    text: `Prepared for TCS Delivery Leadership • ${dateText}`,
    fontSize: 12,
    color: "#94A3B8",
    align: "right",
  });

  return slide;
}

