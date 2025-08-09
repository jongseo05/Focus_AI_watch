-- Supabase Edge Functions가 참조할 테이블 구조

-- 1. 사용자 연결 코드 테이블
CREATE TABLE IF NOT EXISTS watch_codes (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  code TEXT NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  is_used BOOLEAN DEFAULT false,
  used_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. 워치 연결 정보 테이블
CREATE TABLE IF NOT EXISTS watch_connections (
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  device_id TEXT NOT NULL,
  device_type TEXT DEFAULT 'watch',
  last_seen_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  created_at TIMESTAMPTZ DEFAULT NOW(),
  PRIMARY KEY (user_id, device_id)
);

-- 3. 집중 세션 테이블
CREATE TABLE IF NOT EXISTS focus_sessions (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  status TEXT DEFAULT 'active' CHECK (status IN ('active', 'completed', 'cancelled')),
  device_type TEXT DEFAULT 'watch',
  started_at TIMESTAMPTZ DEFAULT NOW(),
  ended_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. 센서 샘플 데이터 테이블
CREATE TABLE IF NOT EXISTS focus_samples (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  session_id UUID REFERENCES focus_sessions(id) ON DELETE CASCADE,
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  heart_rate_mean FLOAT,
  heart_rate_std FLOAT,
  accelerometer_rms FLOAT,
  accelerometer_std FLOAT,
  sample_timestamp TIMESTAMPTZ DEFAULT NOW(),
  device_type TEXT DEFAULT 'watch',
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. RLS 정책 설정
ALTER TABLE watch_codes ENABLE ROW LEVEL SECURITY;
ALTER TABLE watch_connections ENABLE ROW LEVEL SECURITY;
ALTER TABLE focus_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE focus_samples ENABLE ROW LEVEL SECURITY;

-- watch_codes는 SERVICE_ROLE로만 접근 (verify_code에서 사용)
CREATE POLICY "Service role can manage watch_codes" ON watch_codes
  FOR ALL USING (auth.role() = 'service_role');

-- watch_connections은 사용자별 접근
CREATE POLICY "Users can manage their own connections" ON watch_connections
  FOR ALL USING (auth.uid() = user_id);

-- focus_sessions는 사용자별 접근
CREATE POLICY "Users can manage their own sessions" ON focus_sessions
  FOR ALL USING (auth.uid() = user_id);

-- focus_samples는 사용자별 접근
CREATE POLICY "Users can manage their own samples" ON focus_samples
  FOR ALL USING (auth.uid() = user_id);

-- 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_watch_codes_code ON watch_codes(code) WHERE NOT is_used;
CREATE INDEX IF NOT EXISTS idx_focus_sessions_user_status ON focus_sessions(user_id, status);
CREATE INDEX IF NOT EXISTS idx_focus_samples_session ON focus_samples(session_id);
