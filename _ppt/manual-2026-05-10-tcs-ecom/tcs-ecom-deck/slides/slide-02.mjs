export default async function addSlide(presentation, ctx) {
  const slide = presentation.slides.add();

  const colors = {
    ink: "#0F172A",
    muted: "#475569",
    navy: "#0B2E4A",
    teal: "#00A3A3",
    bg: "#FFFFFF",
    card: "#F8FAFC",
    border: "#E2E8F0",
  };

  ctx.addShape(slide, { x: 0, y: 0, w: ctx.W, h: ctx.H, fill: colors.bg });
  ctx.addShape(slide, { x: 0, y: 0, w: ctx.W, h: 10, fill: colors.teal });

  ctx.addText(slide, {
    x: 56,
    y: 34,
    w: ctx.W - 112,
    h: 44,
    text: "Project Overview",
    fontSize: 30,
    bold: true,
    color: colors.navy,
    typeface: ctx.fonts.title,
  });

  ctx.addText(slide, {
    x: 56,
    y: 78,
    w: ctx.W - 112,
    h: 28,
    text: "Goal: build a scalable e‑commerce backend with independent services and a unified API entry point.",
    fontSize: 16,
    color: colors.muted,
  });

  const leftX = 56;
  const topY = 140;
  const colW = (ctx.W - 56 * 2 - 24) / 2;
  const cardH = 470;

  ctx.addShape(slide, { x: leftX, y: topY, w: colW, h: cardH, fill: colors.card, line: ctx.line(colors.border, 1) });
  ctx.addShape(slide, { x: leftX + colW + 24, y: topY, w: colW, h: cardH, fill: colors.card, line: ctx.line(colors.border, 1) });

  ctx.addText(slide, {
    x: leftX + 24,
    y: topY + 22,
    w: colW - 48,
    h: 28,
    text: "Why Microservices",
    fontSize: 18,
    bold: true,
    color: colors.ink,
  });

  const why = [
    "Independent deployability per domain",
    "Scalability per workload (catalog vs orders)",
    "Fault isolation with resilience patterns",
    "Clear ownership boundaries and clean APIs",
  ];
  ctx.addText(slide, {
    x: leftX + 24,
    y: topY + 62,
    w: colW - 48,
    h: 210,
    text: why.map((v) => `• ${v}`).join("\n"),
    fontSize: 15,
    color: colors.ink,
  });

  ctx.addText(slide, {
    x: leftX + 24,
    y: topY + 290,
    w: colW - 48,
    h: 28,
    text: "Deliverables",
    fontSize: 18,
    bold: true,
    color: colors.ink,
  });
  const deliver = [
    "API Gateway as frontdoor",
    "Auth service issues JWT + refresh flow",
    "Product/Catalog + Categories",
    "Order + Cart + Address flows",
    "Payment service integration (PayPal)",
  ];
  ctx.addText(slide, {
    x: leftX + 24,
    y: topY + 330,
    w: colW - 48,
    h: 190,
    text: deliver.map((v) => `• ${v}`).join("\n"),
    fontSize: 15,
    color: colors.ink,
  });

  const rightX = leftX + colW + 24;
  ctx.addText(slide, {
    x: rightX + 24,
    y: topY + 22,
    w: colW - 48,
    h: 28,
    text: "Non‑Functional Targets",
    fontSize: 18,
    bold: true,
    color: colors.ink,
  });
  const nfr = [
    "Low-latency routing via gateway",
    "Stateless auth with JWT + caching",
    "Service discovery (Eureka) and config",
    "Observability readiness (Actuator, logs)",
  ];
  ctx.addText(slide, {
    x: rightX + 24,
    y: topY + 62,
    w: colW - 48,
    h: 170,
    text: nfr.map((v) => `• ${v}`).join("\n"),
    fontSize: 15,
    color: colors.ink,
  });

  ctx.addText(slide, {
    x: rightX + 24,
    y: topY + 252,
    w: colW - 48,
    h: 28,
    text: "Tech Stack (high level)",
    fontSize: 18,
    bold: true,
    color: colors.ink,
  });
  const stack = [
    "Backend: Java 17, Spring Boot 3.2.x",
    "Spring Cloud: Gateway, Eureka, Config",
    "Data: PostgreSQL, Redis",
    "Frontend: React + Vite",
  ];
  ctx.addText(slide, {
    x: rightX + 24,
    y: topY + 292,
    w: colW - 48,
    h: 190,
    text: stack.map((v) => `• ${v}`).join("\n"),
    fontSize: 15,
    color: colors.ink,
  });

  ctx.addText(slide, {
    x: 56,
    y: ctx.H - 36,
    w: ctx.W - 112,
    h: 22,
    text: "Slide 2 / 7",
    fontSize: 11,
    color: "#94A3B8",
    align: "right",
  });

  return slide;
}
