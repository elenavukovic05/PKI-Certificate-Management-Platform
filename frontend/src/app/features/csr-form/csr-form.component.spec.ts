import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CsrFormComponent } from './csr-form.component';

describe('CsrFormComponent', () => {
  let component: CsrFormComponent;
  let fixture: ComponentFixture<CsrFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CsrFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CsrFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
